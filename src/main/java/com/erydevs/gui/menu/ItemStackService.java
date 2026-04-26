package com.erydevs.gui.menu;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import com.erydevs.gui.Entry;
import com.erydevs.config.ButtonConfig;
import com.erydevs.gui.panels.PanelService;
import com.erydevs.placeholders.PlaceholderAPIHook;
import java.util.*;
import java.util.stream.Collectors;

public class ItemStackService {
    private final EryBuyer plugin;
    private final PanelService panelService;

    public ItemStackService(EryBuyer plugin) {
        this.plugin = plugin;
        this.panelService = new PanelService();
    }

    public ItemStack createItemStack(Entry entry, Player player) {
        ItemStack is = new ItemStack(entry.material);
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
        ItemStack exit = new ItemStack(button.getMaterial());
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
        ItemStack autobuyer = new ItemStack(button.getMaterial());
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

