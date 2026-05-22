package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class CloseAction implements Actions {

    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.equalsIgnoreCase("[close]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        player.closeInventory();
    }
}
