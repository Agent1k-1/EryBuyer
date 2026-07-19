package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CloseAction implements Actions {

    @Override
    public boolean canHandle(@NotNull String actionLine) {
        return actionLine.equalsIgnoreCase("[close]");
    }

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        player.closeInventory();
    }
}
