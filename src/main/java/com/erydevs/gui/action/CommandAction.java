package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class CommandAction implements ActionHandler {
    
    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.trim().toLowerCase().startsWith("[command]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String[] parts = actionLine.split("\\s+", 2);
        String command = parts.length > 1 ? parts[1].trim() : "";
        if (!command.isEmpty()) {
            plugin.getServer().dispatchCommand(player, command);
            String menuTitle = player.getOpenInventory().getTitle();
            String menuName = plugin.getBuyerGUI().getMenuNameByTitle(menuTitle);
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName)), 1L);
        }
    }
}
