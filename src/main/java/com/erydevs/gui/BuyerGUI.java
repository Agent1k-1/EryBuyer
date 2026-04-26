package com.erydevs.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configuration;
import com.erydevs.config.ButtonConfig;
import com.erydevs.gui.menu.MenuRegistry;
import com.erydevs.gui.menu.MenuLoaderService;
import com.erydevs.gui.menu.ItemStackService;
import com.erydevs.utils.HexUtils;
import java.util.*;
import java.util.Optional;

public class BuyerGUI {
    private final EryBuyer plugin;
    private final Configuration configManager;
    private final MenuRegistry menuRegistry;
    private final MenuLoaderService menuLoader;
    private final ItemStackService itemStackFactory;
    private final Map<Integer, Entry> combinedSlotMap = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByTitle = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByTitle = new HashMap<>();
    private final Map<String, String> menuNameByTitle = new HashMap<>();

    public BuyerGUI(EryBuyer plugin, Configuration configManager, MenuRegistry menuRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.menuRegistry = menuRegistry;
        this.menuLoader = new MenuLoaderService(plugin, combinedSlotMap, entriesByTitle, actionsByTitle, menuNameByTitle);
        this.itemStackFactory = new ItemStackService(plugin);
        menuLoader.loadAllMenus();
    }

    public Inventory createInventory(Player player, String menuName) {
        FileConfiguration cfg = menuRegistry.getMenuConfig(menuName);
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);

        itemStackFactory.addPanels(inv, cfg, size);
        
        Map<Integer, ButtonConfig> buttons = loadButtons(cfg);
        
        for (Map.Entry<Integer, ButtonConfig> entry : buttons.entrySet()) {
            ButtonConfig btn = entry.getValue();
            if (btn.getSlot() >= 0 && btn.getSlot() < size) {
                ItemStack item = null;
                if (btn.getId().equals("1")) {
                    item = itemStackFactory.createExitItem(btn);
                } else if (btn.hasAction("[command] autobuyer")) {
                    item = itemStackFactory.createAutobuyerItem(btn, player);
                }
                if (item != null) {
                    inv.setItem(btn.getSlot(), item);
                }
            }
        }
        
        addItems(inv, cfg, title, size, player);

        return inv;
    }

    private void addItems(Inventory inv, FileConfiguration cfg, String title, int size, Player player) {
        Map<Integer, Entry> entries = new HashMap<>();
        loadItemSettings(cfg, entries);

        Map<Integer, ButtonConfig> buttons = loadButtons(cfg);
        for (Map.Entry<Integer, ButtonConfig> entry : buttons.entrySet()) {
            ButtonConfig btn = entry.getValue();
            Entry it = new Entry(btn.getId(), btn.getMaterial(), btn.getName(), btn.getLore(), 0.0, 0.0, btn.getSlot());
            if (it.slot < size) {
                entries.put(it.slot, it);
            }
        }

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
            Material m = Material.matchMaterial(cfg.getString(path + ".material"));
            int slot = cfg.getInt(path + ".slot");
            if (m != null && slot >= 0) {
                Entry e = new Entry(key, m, cfg.getString(path + ".name"),
                        cfg.getStringList(path + ".lore"), cfg.getDouble(path + ".prince-x1"),
                        cfg.getDouble(path + ".prince-x64"), slot);
                entries.put(slot, e);
                combinedSlotMap.put(combinedSlotMap.size(), e);
            }
        }
    }

    public Inventory createInventory(Player player) {
        return createInventory(player, "menu");
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

    public boolean isManagedTitle(String title) {
        return entriesByTitle.containsKey(title);
    }

    
    public Map<Integer, ButtonConfig> loadButtons(FileConfiguration cfg) {
        Map<Integer, ButtonConfig> buttons = new HashMap<>();
        loadButtonsSection(cfg, "menu-settings", buttons);
        loadButtonsSection(cfg, "knops-settings", buttons);
        return buttons;
    }

    private void loadButtonsSection(FileConfiguration cfg, String section, Map<Integer, ButtonConfig> buttons) {
        if (!cfg.isConfigurationSection(section)) return;

        org.bukkit.configuration.ConfigurationSection sect = cfg.getConfigurationSection(section);
        if (sect == null) return;

        for (String key : sect.getKeys(false)) {
            String path = section + "." + key;
            if (!cfg.contains(path + ".slot")) continue;

            int slot = cfg.getInt(path + ".slot");
            if (slot < 0) continue;

            Material material = Material.matchMaterial(cfg.getString(path + ".material", ""));
            if (material == null) continue;

            String name = cfg.getString(path + ".name", "");
            List<String> lore = cfg.getStringList(path + ".lore");
            List<String> actions = cfg.getStringList(path + ".action");

            if ((actions == null || actions.isEmpty()) && cfg.contains(path + ".action")) {
                String single = cfg.getString(path + ".action");
                if (single != null) {
                    actions = Collections.singletonList(single);
                }
            }

            ButtonConfig btn = new ButtonConfig(key, slot, material, name, lore, actions);
            buttons.put(slot, btn);
        }
    }

    public Optional<ButtonConfig> findButtonByAction(FileConfiguration cfg, String actionFragment) {
        return loadButtons(cfg).values().stream()
            .filter(btn -> btn.hasAction(actionFragment))
            .findFirst();
    }

    public Optional<ButtonConfig> findButtonById(FileConfiguration cfg, String id) {
        return loadButtons(cfg).values().stream()
            .filter(btn -> btn.getId().equals(id))
            .findFirst();
    }

    public int findSlotByAction(FileConfiguration cfg, String actionFragment) {
        return findButtonByAction(cfg, actionFragment)
            .map(ButtonConfig::getSlot)
            .orElse(-1);
    }

    public int findSlotById(FileConfiguration cfg, String id) {
        return findButtonById(cfg, id)
            .map(ButtonConfig::getSlot)
            .orElse(-1);
    }

    public List<String> getActions(String title, int slot) {
        Map<Integer, List<String>> m = actionsByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    public String getMenuNameByTitle(String title) {
        return menuNameByTitle.getOrDefault(title, "menu");
    }

    public int getExitSlot(String title, FileConfiguration cfg) {
        return findSlotById(cfg, "1");
    }

    public int getAutobuyerSlot(String title, FileConfiguration cfg) {
        return findSlotByAction(cfg, "[command] autobuyer");
    }

    public void reloadMenus() {
        combinedSlotMap.clear();
        entriesByTitle.clear();
        actionsByTitle.clear();
        menuNameByTitle.clear();
        menuLoader.loadAllMenus();
    }
}