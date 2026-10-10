package com.erydevs.folia.impl;

import com.erydevs.folia.Scheduler;
import com.erydevs.folia.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaSchedulerImpl implements Scheduler {

    private static final String PACKAGE = "io.papermc.paper.threadedregions.scheduler.";
    private static final long MILLIS_PER_TICK = 50L;

    private final Plugin plugin;
    private final Object globalScheduler;
    private final Object asyncScheduler;
    private final Method getEntityScheduler;
    private final Method globalRunAtFixedRate;
    private final Method asyncRunNow;
    private final Method asyncRunAtFixedRate;
    private final Method entityRun;
    private final Method entityRunDelayed;
    private final Method taskCancel;

    public FoliaSchedulerImpl(@NotNull Plugin plugin) {
        this.plugin = plugin;
        try {
            Class<?> globalType = Class.forName(PACKAGE + "GlobalRegionScheduler");
            Class<?> asyncType = Class.forName(PACKAGE + "AsyncScheduler");
            Class<?> entityType = Class.forName(PACKAGE + "EntityScheduler");
            Class<?> taskType = Class.forName(PACKAGE + "ScheduledTask");

            globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            getEntityScheduler = Entity.class.getMethod("getScheduler");

            globalRunAtFixedRate = globalType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            asyncRunNow = asyncType.getMethod("runNow", Plugin.class, Consumer.class);
            asyncRunAtFixedRate = asyncType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            entityRun = entityType.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            entityRunDelayed = entityType.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            taskCancel = taskType.getMethod("cancel");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось подключить планировщик Folia", e);
        }
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        call(asyncRunNow, asyncScheduler, plugin, wrap(task));
    }

    @Override
    public @NotNull SchedulerTask runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return handle(call(asyncRunAtFixedRate, asyncScheduler, plugin, wrap(task),
                ticksToMillis(delayTicks), ticksToMillis(periodTicks), TimeUnit.MILLISECONDS));
    }

    @Override
    public @NotNull SchedulerTask runGlobalTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return handle(call(globalRunAtFixedRate, globalScheduler, plugin, wrap(task),
                Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
    }

    @Override
    public void runForPlayer(@NotNull Player player, @NotNull Runnable task) {
        call(entityRun, entityScheduler(player), plugin, wrap(task), null);
    }

    @Override
    public void runForPlayerLater(@NotNull Player player, @NotNull Runnable task, long delayTicks) {
        call(entityRunDelayed, entityScheduler(player), plugin, wrap(task), null, Math.max(1L, delayTicks));
    }

    @NotNull
    private Object entityScheduler(@NotNull Player player) {
        return call(getEntityScheduler, player);
    }

    @NotNull
    private static Consumer<Object> wrap(@NotNull Runnable task) {
        return ignored -> task.run();
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(1L, ticks) * MILLIS_PER_TICK;
    }

    @NotNull
    private SchedulerTask handle(Object scheduledTask) {
        return () -> {
            if (scheduledTask == null) return;
            call(taskCancel, scheduledTask);
        };
    }

    private static Object call(@NotNull Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Ошибка вызова планировщика Folia: " + method.getName(), e);
        }
    }
}
