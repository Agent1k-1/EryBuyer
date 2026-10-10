package com.erydevs.action.impl;

import com.erydevs.EryBuyer;
import com.erydevs.action.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {

    private static final String SHOP_KEYWORD = "shop";

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        String menuName = parseArgument(actionLine);
        if (menuName.isEmpty()) menuName = "menu";

        if (menuName.equalsIgnoreCase(SHOP_KEYWORD)) {
            openShop(plugin, player);
            return;
        }

        String finalMenuName = menuName;
        plugin.getScheduler().runForPlayer(player,
                () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, finalMenuName)));
    }

    private void openShop(@NotNull EryBuyer plugin, @NotNull Player player) {
        if (!plugin.getShopManager().isEnabled()) return;

        String path = plugin.getShopManager().getFirstShopPath();
        if (path == null) return;

        plugin.getScheduler().runForPlayer(player, () -> plugin.getShopGUI().open(player, path));
    }
}
