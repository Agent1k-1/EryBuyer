package com.erydevs.gui.service;

import com.erydevs.EryBuyer;
import com.erydevs.gui.entry.Entry;
import com.erydevs.gui.panel.PanelConfig;
import com.erydevs.gui.panel.PanelLoader;
import com.erydevs.papi.Placeholders;
import com.erydevs.utils.head.SkullUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemStackService {

    private final EryBuyer plugin;

    public ItemStackService(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public ItemStack createItemStack(@NotNull Entry entry, @NotNull Player player) {
        ItemStack item = createBaseItem(entry.material, entry.materialData);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Placeholders.apply(entry.name, player, entry, 1));

            List<String> lore = entry.lore == null ? Collections.emptyList() : entry.lore;
            meta.setLore(lore.stream()
                    .map(line -> Placeholders.apply(line, player, entry, 1))
                    .collect(Collectors.toList()));

            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    private ItemStack createBaseItem(@NotNull Material material, @Nullable String headTextureBase64) {
        if (material == Material.PLAYER_HEAD && headTextureBase64 != null && !headTextureBase64.isEmpty()) {
            return SkullUtils.getSkullByBase64(plugin, headTextureBase64);
        }
        return new ItemStack(material);
    }

    public void addPanels(@NotNull Inventory inv, @NotNull FileConfiguration cfg, int size) {
        for (PanelConfig panel : PanelLoader.load(cfg)) {
            ItemStack item = createPanelItem(panel);
            for (int slot : panel.getSlots()) {
                if (slot < size) inv.setItem(slot, item.clone());
            }
        }
    }

    @NotNull
    private ItemStack createPanelItem(@NotNull PanelConfig panel) {
        ItemStack item = createBaseItem(panel.getMaterial(), panel.getMaterialData());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (panel.getName() != null) meta.setDisplayName(panel.getName());
            meta.setLore(panel.getLore());
            item.setItemMeta(meta);
        }

        return item;
    }
}
