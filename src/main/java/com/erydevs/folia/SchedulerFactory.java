package com.erydevs.folia;

import com.erydevs.folia.impl.BukkitSchedulerImpl;
import com.erydevs.folia.impl.FoliaSchedulerImpl;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SchedulerFactory {

    private SchedulerFactory() {
    }

    @NotNull
    public static Scheduler create(@NotNull Plugin plugin) {
        return FoliaDetector.isFolia() ? new FoliaSchedulerImpl(plugin) : new BukkitSchedulerImpl(plugin);
    }
}
