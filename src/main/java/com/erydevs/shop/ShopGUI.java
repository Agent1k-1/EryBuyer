package com.erydevs.shop;

import com.erydevs.EryBuyer;
import com.erydevs.gui.panel.PanelConfig;
import com.erydevs.shop.file.ShopFile;
import com.erydevs.shop.item.ShopItem;
import com.erydevs.utils.HexUtils;
import com.erydevs.utils.head.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShopGUI {

    private final EryBuyer plugin;
    private final Map<UUID, Inventory> openInventories = new ConcurrentHashMap<>();
    private final Map<UUID, String> openShopPath = new ConcurrentHashMap<>();

    public ShopGUI(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void open(@NotNull Player player, @NotNull String path) {
        ShopFile shop = plugin.getShopManager().getShop(path);
        if (shop == null) return;

        Inventory inventory = Bukkit.createInventory(null, shop.getSize(), HexUtils.colorize(shop.getName()));

        for (PanelConfig panel : shop.getPanels()) {
            ItemStack panelStack = buildPanelStack(panel);
            for (int slot : panel.getSlots()) {
                if (slot < shop.getSize()) inventory.setItem(slot, panelStack.clone());
            }
        }

        for (ShopItem item : shop.getItems().values()) {
            if (item.getSlot() < shop.getSize()) {
                inventory.setItem(item.getSlot(), buildItemStack(item));
            }
        }

        openInventories.put(player.getUniqueId(), inventory);
        openShopPath.put(player.getUniqueId(), path);
        player.openInventory(inventory);
    }

    @Nullable
    public Inventory getOpenInventory(@NotNull Player player) {
        return openInventories.get(player.getUniqueId());
    }

    @Nullable
    public String getOpenShopPath(@NotNull Player player) {
        return openShopPath.get(player.getUniqueId());
    }

    public void forget(@NotNull Player player) {
        openInventories.remove(player.getUniqueId());
        openShopPath.remove(player.getUniqueId());
    }

    @NotNull
    private ItemStack buildPanelStack(@NotNull PanelConfig panel) {
        ItemStack stack = createBaseItem(panel.getMaterial(), panel.getMaterialData());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (panel.getName() != null) meta.setDisplayName(panel.getName());
            meta.setLore(panel.getLore());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @NotNull
    private ItemStack createBaseItem(@NotNull Material material, @Nullable String headTextureBase64) {
        if (material == Material.PLAYER_HEAD && headTextureBase64 != null && !headTextureBase64.isEmpty()) {
            return SkullUtils.getSkullByBase64(plugin, headTextureBase64);
        }
        return new ItemStack(material);
    }

    @NotNull
    private ItemStack buildItemStack(@NotNull ShopItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(HexUtils.colorize(item.getName()));
            List<String> lore = item.getLore().stream()
                    .map(HexUtils::colorize)
                    .collect(Collectors.toList());
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
