package com.erydevs.gui.menu;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import com.erydevs.gui.Entry;
import java.util.*;

public class MenuLoader {
    private final EryBuyer plugin;
    private final Map<Integer, Entry> combinedSlotMap;
    private final Map<String, Map<Integer, Entry>> entriesByTitle;
    private final Map<String, Map<Integer, List<String>>> actionsByTitle;
    private final Map<String, Integer> exitSlotByTitle;
    private final Map<String, Integer> autobuyerSlotByTitle;
    private final Map<String, String> menuNameByTitle;

    public MenuLoader(EryBuyer plugin, Map<Integer, Entry> combinedSlotMap,
                      Map<String, Map<Integer, Entry>> entriesByTitle,
                      Map<String, Map<Integer, List<String>>> actionsByTitle,
                      Map<String, Integer> exitSlotByTitle, Map<String, Integer> autobuyerSlotByTitle,
                      Map<String, String> menuNameByTitle) {
        this.plugin = plugin;
        this.combinedSlotMap = combinedSlotMap;
        this.entriesByTitle = entriesByTitle;
        this.actionsByTitle = actionsByTitle;
        this.exitSlotByTitle = exitSlotByTitle;
        this.autobuyerSlotByTitle = autobuyerSlotByTitle;
        this.menuNameByTitle = menuNameByTitle;
    }

    public void loadAllMenus() {
        List<String> register = plugin.getConfigManager().getConfig().getStringList("register-menu");
        if (register == null) return;
        for (String path : register) {
            if (path == null || path.trim().isEmpty()) continue;
            java.io.File f = new java.io.File(plugin.getDataFolder(), path);
            if (!f.exists()) continue;
            loadMenu(f);
        }
    }

    private void loadMenu(java.io.File f) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        String menuName = f.getName().replace(".yml", "");
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        menuNameByTitle.put(title, menuName);
        exitSlotByTitle.put(title, cfg.getInt("knops-settings.1.slot"));
        autobuyerSlotByTitle.put(title, cfg.getInt("knops-settings.2.slot"));

        Map<Integer, Entry> entries = new HashMap<>();
        Map<Integer, List<String>> actions = new HashMap<>();
        loadItemSettings(cfg, entries);
        loadMenuSettings(cfg, entries, actions);
        loadKnopsSettings(cfg, entries, actions);

        entriesByTitle.put(title, entries);
        actionsByTitle.put(title, actions);
    }

    private void loadItemSettings(FileConfiguration cfg, Map<Integer, Entry> entries) {
        if (!cfg.isConfigurationSection("item-settings")) return;
        for (String key : cfg.getConfigurationSection("item-settings").getKeys(false)) {
            String path = "item-settings." + key;
            Material m = Material.matchMaterial(cfg.getString(path + ".material"));
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
        Material m = Material.matchMaterial(cfg.getString(path + ".material"));
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
}
