package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        String menuName = parseArgument(actionLine);
        if (menuName.isEmpty()) menuName = "menu";
        String finalMenuName = menuName;
        plugin.getServer().getScheduler().runTask(plugin,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, finalMenuName)));
    }
}
