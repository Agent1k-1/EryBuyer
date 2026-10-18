package com.erydevs.shop.listener;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.shop.file.ShopFile;
import com.erydevs.shop.item.ShopItem;
import com.erydevs.shop.type.ShopItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopClickListener implements Listener {

    private final EryBuyer plugin;

    public ShopClickListener(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        Inventory tracked = plugin.getShopGUI().getOpenInventory(player);
        if (tracked == null || e.getView().getTopInventory() != tracked) return;

        int slot = e.getRawSlot();
        if (slot < 0) return;

        e.setCancelled(true);
        if (slot >= tracked.getSize()) return;
        if (!e.getClick().isLeftClick() && !e.getClick().isRightClick()) return;

        String path = plugin.getShopGUI().getOpenShopPath(player);
        if (path == null) return;

        ShopFile shop = plugin.getShopManager().getShop(path);
        if (shop == null) return;

        ShopItem item = findBySlot(shop, slot);
        if (item == null) return;

        purchase(player, item);
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        Inventory tracked = plugin.getShopGUI().getOpenInventory(player);
        if (tracked != null && tracked.equals(e.getInventory())) {
            plugin.getShopGUI().forget(player);
        }
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory tracked = plugin.getShopGUI().getOpenInventory((Player) event.getWhoClicked());
        if (tracked != null && event.getView().getTopInventory() == tracked) event.setCancelled(true);
    }

    @Nullable
    private ShopItem findBySlot(@NotNull ShopFile shop, int slot) {
        for (ShopItem item : shop.getItems().values()) {
            if (item.getSlot() == slot) return item;
        }
        return null;
    }

    private void purchase(@NotNull Player player, @NotNull ShopItem item) {
        if (item.getType() == null && item.getPrice() == 0) {
            plugin.getScheduler().runForPlayer(player, () -> ActionType.dispatchAll(plugin, player, item.getActions()));
            return;
        }
        boolean temporaryBooster = item.getType() == ShopItemType.TEMP_MONEY_BOOST || item.getType() == ShopItemType.TEMP_POINTS_BOOST;
        if (temporaryBooster && plugin.getShopManager().hasPurchasedBooster(player.getUniqueId(), item.getId())) {
            java.util.List<String> messages = plugin.getMessagesConfig().getMessageErrorBoosterBuy().stream()
                    .map(line -> com.erydevs.papi.Placeholders.apply(line, player))
                    .collect(java.util.stream.Collectors.toList());
            ActionType.dispatchAll(plugin, player, messages);
            return;
        }
        if (!plugin.getShopManager().hasTokens(player.getUniqueId(), item.getPrice())) {
            ActionType.dispatchAll(plugin, player, plugin.getMessagesConfig().getMessageNoTokens());
            return;
        }

        plugin.getShopManager().takeTokens(player.getUniqueId(), item.getPrice());
        applyEffect(player, item);
        if (temporaryBooster) {
            plugin.getShopManager().rememberPurchasedBooster(player.getUniqueId(), item);
            java.util.List<String> messages = plugin.getMessagesConfig().getMessageBoosterBuy().stream()
                    .map(line -> com.erydevs.papi.Placeholders.apply(line, player))
                    .collect(java.util.stream.Collectors.toList());
            ActionType.dispatchAll(plugin, player, messages);
        }
        ActionType.dispatchAll(plugin, player, item.getActions());
    }

    private void applyEffect(@NotNull Player player, @NotNull ShopItem item) {
        if (item.getType() == null) return;

        if (item.getType() == ShopItemType.RESET_BEST_ROTATION) {
            if (plugin.getBestManager() != null) {
                plugin.getBestManager().rotate();
            }
        }
    }
}
