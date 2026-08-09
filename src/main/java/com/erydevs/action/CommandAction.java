package com.erydevs.action;

import com.erydevs.EryBuyer;
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
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName)), 1L);
    }
}
