package com.erydevs.action.impl;

import com.erydevs.EryBuyer;
import com.erydevs.action.Action;
import com.erydevs.action.ActionType;
import org.bukkit.entity.Player;

public final class AutoBuyerItemsAction implements Action {
    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        if (!player.hasPermission("erybuyer.autobuyer")) {
            ActionType.dispatchAll(plugin, player, plugin.getMessagesConfig().getMessageNoPermission());
            return;
        }
        String argument = parseArgument(actionLine);
        if (!argument.equalsIgnoreCase("enable") && !argument.equalsIgnoreCase("disable")) return;
        boolean enabled = argument.equalsIgnoreCase("enable");
        plugin.getAutoBuyerSettings().runForPlayer(player, () -> {
            plugin.getAutoBuyerSettings().setAllItemsEnabled(player.getUniqueId(), enabled);
            plugin.getAutoBuyerManager().markDirty(player);
            plugin.getBuyerGUI().refreshOpenInventory(player);
        });
    }
}
