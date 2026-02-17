package com.erydevs.gui;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configuration;
import com.erydevs.gui.menu.MenuLoader;
import com.erydevs.gui.menu.ItemStackFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class BuyerGUI {
    private final EryBuyer plugin;
    private final Configuration configManager;
    private final MenuLoader menuLoader;
    private final ItemStackFactory itemStackFactory;
    private final Map<Integer, Entry> combinedSlotMap = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByTitle = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByTitle = new HashMap<>();
    private final Map<String, Integer> exitSlotByTitle = new HashMap<>();
    private final Map<String, Integer> autobuyerSlotByTitle = new HashMap<>();
    private final Map<String, String> menuNameByTitle = new HashMap<>();

    public BuyerGUI(EryBuyer plugin, Configuration configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.menuLoader = new MenuLoader(plugin, combinedSlotMap, entriesByTitle, actionsByTitle, 
                                         exitSlotByTitle, autobuyerSlotByTitle, menuNameByTitle);
        this.itemStackFactory = new ItemStackFactory(plugin);
        menuLoader.loadAllMenus();
    }

    public Inventory createInventory(Player player, String menuName) {
        FileConfiguration cfg = configManager.getMenuConfig(menuName);
        com.erydevs.utils.HexUtils hexUtils = new com.erydevs.utils.HexUtils();
        String title = com.erydevs.utils.HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);

        itemStackFactory.addPanels(inv, cfg, size);
        addItems(inv, cfg, title, size, player);

        int exitSlot = cfg.getInt("knops-settings.1.slot");
        if (exitSlot < size) inv.setItem(exitSlot, itemStackFactory.createExitItem(cfg));

        if (menuName.equals("menu")) {
            int autobuyerSlot = cfg.getInt("knops-settings.2.slot");
            if (autobuyerSlot < size) inv.setItem(autobuyerSlot, itemStackFactory.createAutobuyerItem(cfg, player));
        }

        return inv;
    }

    private void addItems(Inventory inv, FileConfiguration cfg, String title, int size, Player player) {
        Map<Integer, Entry> entries = new HashMap<>();
        loadItemSettings(cfg, entries);
        loadMenuSettings(cfg, entries, new HashMap<>());
        loadKnopsSettings(cfg, entries, new HashMap<>());

        for (Map.Entry<Integer, Entry> e : entries.entrySet()) {
            Entry it = e.getValue();
            if (it.slot < size) inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
        }
        entriesByTitle.put(title, entries);
    }

    private void loadItemSettings(FileConfiguration cfg, Map<Integer, Entry> entries) {
        if (!cfg.isConfigurationSection("item-settings")) return;
        for (String key : cfg.getConfigurationSection("item-settings").getKeys(false)) {
            String path = "item-settings." + key;
            org.bukkit.Material m = org.bukkit.Material.matchMaterial(cfg.getString(path + ".material"));
            int slot = cfg.getInt(path + ".slot", -1);
            if (m != null && slot >= 0) {
                Entry e = new Entry(key, m, cfg.getString(path + ".name"),
                        cfg.getStringList(path + ".lore"), cfg.getDouble(path + ".prince-x1"),
                        cfg.getDouble(path + ".prince-x64"), slot);
                entries.put(slot, e);
                combinedSlotMap.put(combinedSlotMap.size(), e);
            }
        }
    }

    private void loadMenuSettings(FileConfiguration cfg, Map<Integer, Entry> entries, Map<Integer, List<String>> actions) {
        if (!cfg.isConfigurationSection("menu-settings")) return;
        for (String key : cfg.getConfigurationSection("menu-settings").getKeys(false)) {
            loadMenuOrKnopsItem(cfg, entries, actions, "menu-settings." + key, "menu-" + key);
        }
    }

    private void loadKnopsSettings(FileConfiguration cfg, Map<Integer, Entry> entries, Map<Integer, List<String>> actions) {
        if (!cfg.isConfigurationSection("knops-settings")) return;
        for (String key : cfg.getConfigurationSection("knops-settings").getKeys(false)) {
            loadMenuOrKnopsItem(cfg, entries, actions, "knops-settings." + key, "knop-" + key);
        }
    }

    private void loadMenuOrKnopsItem(FileConfiguration cfg, Map<Integer, Entry> entries, Map<Integer, List<String>> actions, String path, String id) {
        int slot = cfg.getInt(path + ".slot");
        if (slot < 0) return;
        org.bukkit.Material m = org.bukkit.Material.matchMaterial(cfg.getString(path + ".material"));
        if (m != null) {
            Entry e = new Entry(id, m, cfg.getString(path + ".name"), cfg.getStringList(path + ".lore"), 0.0, 0.0, slot);
            entries.put(slot, e);
        }
        List<String> acts = cfg.getStringList(path + ".action");
        if ((acts == null || acts.isEmpty()) && cfg.contains(path + ".action")) {
            String single = cfg.getString(path + ".action");
            if (single != null) acts = Arrays.asList(single);
        }
        if (acts != null && !acts.isEmpty()) actions.put(slot, acts);
    }

    public Inventory createInventory(Player player) {
        return createInventory(player, "menu");
    }

    public boolean isManagedTitle(String title) {
        return entriesByTitle.containsKey(title);
    }

    public Entry getEntry(String title, int slot) {
        Map<Integer, Entry> m = entriesByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    public Map<Integer, Entry> getSlotMap() {
        return combinedSlotMap;
    }

    public Collection<Entry> getAllEntries() {
        List<Entry> out = new ArrayList<>();
        for (Map<Integer, Entry> m : entriesByTitle.values()) {
            out.addAll(m.values());
        }
        return out;
    }

    public int getExitSlot(String title) {
        return exitSlotByTitle.getOrDefault(title, 53);
    }

    public int getAutobuyerSlot(String title) {
        return autobuyerSlotByTitle.getOrDefault(title, 49);
    }

    public List<String> getActions(String title, int slot) {
        Map<Integer, List<String>> m = actionsByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    public String getMenuNameByTitle(String title) {
        return menuNameByTitle.getOrDefault(title, "menu");
    }
}