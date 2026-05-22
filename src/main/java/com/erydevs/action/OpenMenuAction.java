package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public class OpenMenuAction implements Actions {

    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.toLowerCase().startsWith("[openmenu]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String menuName = parseArgument(actionLine);
        if (menuName.isEmpty()) menuName = "menu";
        String finalMenuName = menuName;
        plugin.getServer().getScheduler().runTask(plugin,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, finalMenuName)));
    }
}
