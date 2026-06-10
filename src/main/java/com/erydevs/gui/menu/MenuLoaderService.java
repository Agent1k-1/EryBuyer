package com.erydevs.gui.menu;

import com.erydevs.utils.head.MaterialHeadParser;
import com.erydevs.utils.head.ParsedMaterial;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import com.erydevs.gui.Entry;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuLoaderService {
    private final EryBuyer plugin;
    private final Map<Integer, Entry> combinedSlotMap;
    private final Map<String, Map<Integer, Entry>> entriesByTitle;
    private final Map<String, Map<Integer, List<String>>> actionsByTitle;
    private final Map<String, String> menuNameByTitle;

    public MenuLoaderService(EryBuyer plugin, Map<Integer, Entry> combinedSlotMap,
                      Map<String, Map<Integer, Entry>> entriesByTitle,
                      Map<String, Map<Integer, List<String>>> actionsByTitle,
                      Map<String, String> menuNameByTitle) {
        this.plugin = plugin;
        this.combinedSlotMap = combinedSlotMap;
        this.entriesByTitle = entriesByTitle;
        this.actionsByTitle = actionsByTitle;
        this.menuNameByTitle = menuNameByTitle;
    }

    public void loadAllMenus() {
        List<String> register = plugin.getConfigManager().getRegisterMenu();
        if (register == null) return;
        for (String path : register) {
            if (path == null || path.trim().isEmpty()) continue;
            File f = new File(plugin.getDataFolder(), path);
            if (!f.exists()) continue;
            loadMenu(f);
        }
    }

    private void loadMenu(File f) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        String menuName = f.getName().replace(".yml", "");
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        menuNameByTitle.put(title, menuName);

        Map<Integer, Entry> entries = new HashMap<>();
        Map<Integer, List<String>> actions = new HashMap<>();

        for (Map.Entry<Integer, Entry> e : loadItemSettings(cfg, combinedSlotMap).entrySet()) {
            entries.put(e.getKey(), e.getValue());
        }

        loadMenuSettings(cfg, entries, actions);
        loadKnopsSettings(cfg, entries, actions);

        entriesByTitle.put(title, entries);
        actionsByTitle.put(title, actions);
    }

    public static Map<Integer, Entry> loadItemSettings(FileConfiguration cfg, Map<Integer, Entry> combinedSlotMap) {
        Map<Integer, Entry> entries = new HashMap<>();
        if (!cfg.isConfigurationSection("item-settings")) return entries;

        for (String key : cfg.getConfigurationSection("item-settings").getKeys(false)) {
            String path = "item-settings." + key;
            ParsedMaterial pm = MaterialHeadParser.parse(cfg.getString(path + ".material"));
            int slot = cfg.getInt(path + ".slot");
            if (pm == null || slot < 0) continue;

            Entry entry = pm.isCustomHead()
                    ? new Entry(key, pm.getMaterial(), pm.getHeadTextureBase64(), cfg.getString(path + ".name"),
                    cfg.getStringList(path + ".lore"), cfg.getDouble(path + ".prince-x1"),
                    cfg.getDouble(path + ".prince-x64"), slot)
                    : new Entry(key, pm.getMaterial(), cfg.getString(path + ".name"),
                    cfg.getStringList(path + ".lore"), cfg.getDouble(path + ".prince-x1"),
                    cfg.getDouble(path + ".prince-x64"), slot);

            entries.put(slot, entry);
            if (combinedSlotMap != null) {
                combinedSlotMap.put(combinedSlotMap.size(), entry);
            }
        }

        return entries;
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

        ParsedMaterial pm = MaterialHeadParser.parse(cfg.getString(path + ".material"));
        if (pm != null) {
            Entry entry = pm.isCustomHead()
                    ? new Entry(id, pm.getMaterial(), pm.getHeadTextureBase64(), cfg.getString(path + ".name"),
                    cfg.getStringList(path + ".lore"), 0.0, 0.0, slot)
                    : new Entry(id, pm.getMaterial(), cfg.getString(path + ".name"), cfg.getStringList(path + ".lore"), 0.0, 0.0, slot);
            entries.put(slot, entry);
        }

        List<String> acts = cfg.getStringList(path + ".action");
        if ((acts == null || acts.isEmpty()) && cfg.contains(path + ".action")) {
            String single = cfg.getString(path + ".action");
            if (single != null) acts = Arrays.asList(single);
        }
        if (acts != null && !acts.isEmpty()) actions.put(slot, acts);
    }
}
