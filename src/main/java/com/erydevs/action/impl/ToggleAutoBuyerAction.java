package com.erydevs.action.impl;

import com.erydevs.EryBuyer;
import com.erydevs.action.Action;
import com.erydevs.action.ActionType;
import org.bukkit.entity.Player;

public final class ToggleAutoBuyerAction implements Action {
    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        if (!player.hasPermission("erybuyer.autobuyer")) {
            ActionType.dispatchAll(plugin, player, plugin.getMessagesConfig().getMessageNoPermission());
            return;
        }
        plugin.getAutoBuyerManager().toggleAutobuyer(player);
        plugin.getScheduler().runForPlayer(player, () -> plugin.getBuyerGUI().refreshOpenInventory(player));
    }
}
