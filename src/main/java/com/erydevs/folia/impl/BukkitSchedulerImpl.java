package com.erydevs.folia.impl;

import com.erydevs.folia.Scheduler;
import com.erydevs.folia.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitSchedulerImpl implements Scheduler {

    private final Plugin plugin;

    public BukkitSchedulerImpl(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public @NotNull SchedulerTask runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    @Override
    public @NotNull SchedulerTask runGlobalTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    @Override
    public void runForPlayer(@NotNull Player player, @NotNull Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runForPlayerLater(@NotNull Player player, @NotNull Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
}
