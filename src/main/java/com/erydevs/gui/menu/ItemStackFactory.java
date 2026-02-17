package com.erydevs.gui.menu;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import com.erydevs.gui.Entry;
import com.erydevs.placeholders.BuyerPlaceholder;
import java.util.*;
import java.util.stream.Collectors;

public class ItemStackFactory {
    private final EryBuyer plugin;

    public ItemStackFactory(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItemStack(Entry entry, Player player) {
        ItemStack is = new ItemStack(entry.material);
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            String displayName = BuyerPlaceholder.apply(entry.name, player);
            im.setDisplayName(displayName);
            List<String> lore = entry.lore == null ? Collections.emptyList() : entry.lore;
            List<String> processedLore = lore.stream()
                    .map(line -> BuyerPlaceholder.apply(line, player))
                    .collect(Collectors.toList());
            im.setLore(processedLore);
            is.setItemMeta(im);
        }
        return is;
    }

    public ItemStack createExitItem(FileConfiguration cfg) {
        String materialStr = cfg.getString("knops-settings.1.material");
        Material material = Material.BARRIER;

        if (materialStr != null && !materialStr.isEmpty()) {
            try {
                material = Material.valueOf(materialStr.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        ItemStack exit = new ItemStack(material);
        ItemMeta em = exit.getItemMeta();
        if (em != null) {
            String displayName = cfg.getString("knops-settings.1.name");
            em.setDisplayName(HexUtils.colorize(displayName));
            em.setLore(getButtonLore(cfg, "knops-settings.1.lore"));
            exit.setItemMeta(em);
        }
        return exit;
    }

    public ItemStack createAutobuyerItem(FileConfiguration cfg, Player player) {
        String materialStr = cfg.getString("knops-settings.2.material");
        Material material = Material.LIME_CONCRETE;

        if (materialStr != null && !materialStr.isEmpty()) {
            try {
                material = Material.valueOf(materialStr.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        ItemStack autobuyer = new ItemStack(material);
        ItemMeta am = autobuyer.getItemMeta();
        if (am != null) {
            String displayName = cfg.getString("knops-settings.2.name");
            am.setDisplayName(HexUtils.colorize(displayName));
            String status = plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                    plugin.getConfigManager().getConfig().getString("placeholder.enable-autobuyer"):
                    plugin.getConfigManager().getConfig().getString("placeholder.disable-autobuyer");
            am.setLore(Arrays.asList(HexUtils.colorize(status)));
            autobuyer.setItemMeta(am);
        }
        return autobuyer;
    }

    public void addPanels(Inventory inv, FileConfiguration cfg, int size) {
        if (cfg.isList("panels")) {
            List<?> panelsList = cfg.getList("panels");
            if (panelsList != null) {
                for (Object obj : panelsList) {
                    if (obj instanceof Map) {
                        Map<String, Object> panelMap = (Map<String, Object>) obj;
                        String materialName = (String) panelMap.get("material");
                        String name = (String) panelMap.get("name");
                        List<?> loreList = (List<?>) panelMap.get("lore");
                        List<?> slotsList = (List<?>) panelMap.get("slots");

                        if (materialName != null && slotsList != null) {
                            Material mat = Material.matchMaterial(materialName);
                            if (mat != null) {
                                ItemStack panel = new ItemStack(mat);
                                ItemMeta pm = panel.getItemMeta();
                                if (pm != null) {
                                    if (name != null) {
                                        pm.setDisplayName(HexUtils.colorize(name));
                                    }
                                    List<String> lore = new ArrayList<>();
                                    if (loreList != null) {
                                        for (Object loreObj : loreList) {
                                            String loreLine = loreObj.toString().trim();
                                            lore.add(loreLine.isEmpty() ? " " : HexUtils.colorize(loreLine));
                                        }
                                    }
                                    if (lore.isEmpty()) lore.add(" ");
                                    pm.setLore(lore);
                                    panel.setItemMeta(pm);
                                }
                                for (Object slotObj : slotsList) {
                                    int slot = ((Number) slotObj).intValue();
                                    if (slot < size) inv.setItem(slot, panel.clone());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            String materialName = cfg.getString("panels.material");
            if (materialName != null) {
                Material panelMat = Material.matchMaterial(materialName);
                if (panelMat != null) {
                    ItemStack panel = createPanelItem(cfg, panelMat);
                    List<Integer> slots = cfg.getIntegerList("slots");
                    if (slots != null) {
                        for (int slot : slots) {
                            if (slot < size) inv.setItem(slot, panel.clone());
                        }
                    }
                }
            }
        }
    }

    private ItemStack createPanelItem(FileConfiguration cfg, Material mat) {
        ItemStack panel = new ItemStack(mat);
        ItemMeta pm = panel.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(HexUtils.colorize(cfg.getString("panels.name")));
            List<String> lore = cfg.getStringList("panels.lore").stream()
                    .map(l -> l.trim().isEmpty() ? " " : HexUtils.colorize(l))
                    .collect(Collectors.toList());
            if (lore.isEmpty()) lore.add(" ");
            pm.setLore(lore);
            panel.setItemMeta(pm);
        }
        return panel;
    }

    private List<String> getButtonLore(FileConfiguration cfg, String path) {
        return cfg.getStringList(path).stream()
                .map(l -> l.trim().isEmpty() ? " " : HexUtils.colorize(l))
                .collect(Collectors.toList());
    }
}
