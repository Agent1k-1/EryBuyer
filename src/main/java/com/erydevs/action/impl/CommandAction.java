package com.erydevs.action.impl;

import com.erydevs.EryBuyer;
import com.erydevs.action.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandAction implements Action {

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        String command = parseArgument(actionLine);
        if (command.isEmpty()) return;
        plugin.getServer().dispatchCommand(player, command);
        reopenMenu(plugin, player);
    }

    private void reopenMenu(@NotNull EryBuyer plugin, @NotNull Player player) {
        String title = player.getOpenInventory().getTitle();
        String menuName = plugin.getBuyerGUI().getMenuNameByTitle(title);
        plugin.getScheduler().runForPlayerLater(player,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName)), 1L);
    }
}
