package com.erydevs.gui.service;

import com.erydevs.EryBuyer;
import com.erydevs.gui.entry.Entry;
import com.erydevs.papi.PlaceholderAPIHook;
import com.erydevs.utils.HexUtils;
import com.erydevs.utils.head.MaterialHeadParser;
import com.erydevs.utils.head.ParsedMaterial;
import com.erydevs.utils.head.SkullUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            meta.setDisplayName(PlaceholderAPIHook.apply(entry.name, player, entry, 1));

            List<String> lore = entry.lore == null ? Collections.emptyList() : entry.lore;
            meta.setLore(lore.stream()
                    .map(line -> PlaceholderAPIHook.apply(line, player, entry, 1))
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
        if (!cfg.isList("panels")) return;

        List<?> panelsList = cfg.getList("panels");
        if (panelsList == null) return;

        for (Object obj : panelsList) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> panelMap = (Map<String, Object>) obj;
                addPanel(inv, panelMap, size);
            }
        }
    }

    private void addPanel(@NotNull Inventory inv, @NotNull Map<String, Object> panelMap, int size) {
        String material = (String) panelMap.get("material");
        String name = (String) panelMap.get("name");
        List<?> lore = (List<?>) panelMap.get("lore");
        List<?> slots = (List<?>) panelMap.get("slots");

        if (material == null || slots == null) return;

        ParsedMaterial pm = MaterialHeadParser.parse(material);
        if (pm == null) return;

        List<String> loreList = new ArrayList<>();
        if (lore != null) {
            for (Object line : lore) {
                String colorized = line.toString().trim().isEmpty() ? " " : HexUtils.colorize(line.toString());
                loreList.add(colorized);
            }
        }
        if (loreList.isEmpty()) loreList.add(" ");

        ItemStack item = createBaseItem(pm.getMaterial(), pm.getHeadTextureBase64());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(HexUtils.colorize(name));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }

        for (Object slotObj : slots) {
            int slot = ((Number) slotObj).intValue();
            if (slot < size) {
                inv.setItem(slot, item.clone());
            }
        }
    }
}