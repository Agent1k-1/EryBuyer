package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class OpenMenuAction implements ActionHandler {
    
    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.trim().toLowerCase().startsWith("[openmenu]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String[] parts = actionLine.split("\\s+", 2);
        String menuName = parts.length > 1 ? parts[1].trim() : "menu";
        plugin.getServer().getScheduler().runTask(plugin, () ->
                player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName))
        );
    }
}
