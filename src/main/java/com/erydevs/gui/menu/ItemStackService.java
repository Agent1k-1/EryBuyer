package com.erydevs.gui.menu;

import com.erydevs.EryBuyer;
import com.erydevs.utils.button.ButtonConfig;
import com.erydevs.gui.Entry;
import com.erydevs.gui.panels.PanelService;
import com.erydevs.gui.impl.head.SkullUtils;
import com.erydevs.papi.PlaceholderAPIHook;
import com.erydevs.utils.HexUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemStackService {
    private final EryBuyer plugin;
    private final PanelService panelService;

    public ItemStackService(EryBuyer plugin) {
        this.plugin = plugin;
        this.panelService = new PanelService(plugin);
    }

    public ItemStack createItemStack(Entry entry, Player player) {
        ItemStack is;
        if (entry.material == Material.PLAYER_HEAD && entry.materialData != null && !entry.materialData.isEmpty()) {
            is = SkullUtils.getSkullByBase64(plugin, entry.materialData);
        } else {
            is = new ItemStack(entry.material);
        }
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            String displayName = PlaceholderAPIHook.apply(entry.name, player, entry, 1);
            im.setDisplayName(displayName);
            List<String> lore = entry.lore == null ? Collections.emptyList() : entry.lore;
            List<String> processedLore = lore.stream()
                    .map(line -> PlaceholderAPIHook.apply(line, player, entry, 1))
                    .collect(Collectors.toList());
            im.setLore(processedLore);
            is.setItemMeta(im);
        }
        return is;
    }

    public ItemStack createExitItem(ButtonConfig button) {
        ItemStack exit;
        if (button.getMaterial() == Material.PLAYER_HEAD
                && button.getMaterialStr() != null
                && !button.getMaterialStr().isEmpty()) {
            exit = SkullUtils.getSkullByBase64(plugin, button.getMaterialStr());
        } else {
            exit = new ItemStack(button.getMaterial());
        }
        ItemMeta em = exit.getItemMeta();
        if (em != null) {
            em.setDisplayName(HexUtils.colorize(button.getName()));
            em.setLore(button.getLore().stream()
                    .map(HexUtils::colorize)
                    .collect(Collectors.toList()));
            exit.setItemMeta(em);
        }
        return exit;
    }

    public ItemStack createAutobuyerItem(ButtonConfig button, Player player) {
        ItemStack autobuyer;
        if (button.getMaterial() == Material.PLAYER_HEAD
                && button.getMaterialStr() != null
                && !button.getMaterialStr().isEmpty()) {
            autobuyer = SkullUtils.getSkullByBase64(plugin, button.getMaterialStr());
        } else {
            autobuyer = new ItemStack(button.getMaterial());
        }
        ItemMeta am = autobuyer.getItemMeta();
        if (am != null) {
            am.setDisplayName(HexUtils.colorize(button.getName()));
            
            List<String> lore = new ArrayList<>(button.getLore());
            List<String> processedLore = new ArrayList<>();
            for (String line : lore) {
                String processed = PlaceholderAPIHook.apply(line, player);
                processedLore.add(processed);
            }
            
            if (processedLore.isEmpty()) {
                String status = plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                        plugin.getConfigManager().getPlaceholderEnableAutobuyer():
                        plugin.getConfigManager().getPlaceholderDisableAutobuyer();
                processedLore.add(HexUtils.colorize(status));
            }
            
            am.setLore(processedLore);
            autobuyer.setItemMeta(am);
        }
        return autobuyer;
    }

    public void addPanels(Inventory inv, FileConfiguration cfg, int size) {
        panelService.addPanelsToInventory(inv, cfg, size);
    }
}

