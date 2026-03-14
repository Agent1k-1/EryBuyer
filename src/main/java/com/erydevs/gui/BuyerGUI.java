package com.erydevs.gui;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configuration;
import com.erydevs.gui.menu.ItemStackFactory;
import com.erydevs.gui.menu.MenuCache;
import com.erydevs.gui.menu.MenuLoader;
import com.erydevs.utils.HexUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.List;

public class BuyerGUI {
    private final EryBuyer plugin;
    private final Configuration configManager;
    private final ItemStackFactory itemStackFactory;
    private final MenuCache cache;

    public BuyerGUI(EryBuyer plugin, Configuration configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.itemStackFactory = new ItemStackFactory(plugin);
        this.cache = new MenuCache();

        MenuLoader loader = new MenuLoader(plugin, cache);
        loader.loadAllMenus();
    }

    public Inventory createInventory(Player player, String menuName) {
        FileConfiguration cfg = plugin.getMenuLoad().getMenuConfig(menuName);
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);

        itemStackFactory.addPanels(inv, cfg, size);

        for (Entry entry : cache.getAllEntries()) {
            if (entry.slot < size && entry.priceX1 > 0) {
                inv.setItem(entry.slot, itemStackFactory.createItemStack(entry, player));
            }
        }

        addExitButton(inv, cfg, size);
        addAutobuyerButton(inv, cfg, menuName, size, player);

        return inv;
    }

    public Inventory createInventory(Player player) {
        return createInventory(player, "menu");
    }

    private void addExitButton(Inventory inv, FileConfiguration cfg, int size) {
        if (cfg.contains("knops-settings.1.slot")) {
            int exitSlot = cfg.getInt("knops-settings.1.slot");
            if (exitSlot >= 0 && exitSlot < size) {
                inv.setItem(exitSlot, itemStackFactory.createExitItem(cfg));
            }
        }
    }

    private void addAutobuyerButton(Inventory inv, FileConfiguration cfg, String menuName, int size, Player player) {
        if (!menuName.equals("menu")) return;

        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int autobuyerSlot = cache.getAutobuyerSlot(title);
        
        if (autobuyerSlot >= 0 && autobuyerSlot < size) {
            String path = "knops-settings" + ".slot";
            if (cfg.contains("knops-settings")) {
                for (String key : cfg.getConfigurationSection("knops-settings").getKeys(false)) {
                    if (cfg.getInt("knops-settings." + key + ".slot", -1) == autobuyerSlot) {
                        inv.setItem(autobuyerSlot, itemStackFactory.createAutobuyerItem(cfg, key, player));
                        return;
                    }
                }
            }
        }
    }

    public boolean isManagedTitle(String title) {
        return cache.isManagedTitle(title);
    }

    public Entry getEntry(String title, int slot) {
        return cache.getEntry(title, slot);
    }

    public List<String> getActions(String title, int slot) {
        return cache.getActions(title, slot);
    }

    public int getExitSlot(String title, FileConfiguration cfg) {
        return cache.getExitSlot(title);
    }

    public int getAutobuyerSlot(String title, FileConfiguration cfg) {
        return cache.getAutobuyerSlot(title);
    }

    public String getMenuNameByTitle(String title) {
        return cache.getMenuName(title);
    }

    public Collection<Entry> getAllEntries() {
        return cache.getAllEntries();
    }
}