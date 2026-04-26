package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class CloseAction implements ActionHandler {
    
    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.trim().equalsIgnoreCase("[close]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        player.closeInventory();
    }
}
