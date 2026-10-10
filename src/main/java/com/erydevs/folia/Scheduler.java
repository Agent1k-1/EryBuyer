package com.erydevs.folia;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Scheduler {

    void runAsync(@NotNull Runnable task);

    @NotNull
    SchedulerTask runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    @NotNull
    SchedulerTask runGlobalTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    void runForPlayer(@NotNull Player player, @NotNull Runnable task);

    void runForPlayerLater(@NotNull Player player, @NotNull Runnable task, long delayTicks);
}
