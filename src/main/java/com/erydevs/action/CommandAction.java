package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class CommandAction implements Actions {

    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.toLowerCase().startsWith("[command]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String command = parseArgument(actionLine);
        if (command.isEmpty()) return;
        plugin.getServer().dispatchCommand(player, command);
        reopenMenu(plugin, player);
    }

    private void reopenMenu(EryBuyer plugin, Player player) {
        String title = player.getOpenInventory().getTitle();
        String menuName = plugin.getBuyerGUI().getMenuNameByTitle(title);
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName)), 1L);
    }
}
