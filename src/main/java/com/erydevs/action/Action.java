package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Action {

    void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player);

    @NotNull
    default String parseArgument(@NotNull String actionLine) {
        String[] parts = actionLine.split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }
}
