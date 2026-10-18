package com.erydevs.gui.loader;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuLoader {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> cache = new ConcurrentHashMap<>();

    public MenuLoader(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    @Nullable
    public FileConfiguration getMenuConfig(@NotNull String name) {
        FileConfiguration cached = cache.get(name);
        if (cached != null) return cached;
        return cache.computeIfAbsent(name, this::load);
    }

    @Nullable
    private FileConfiguration load(@NotNull String name) {
        File file = new File(plugin.getDataFolder(), "menu/" + name + ".yml");
        if (!file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveDefaults(@Nullable List<String> paths) {
        if (paths == null) return;
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) continue;
            File f = new File(plugin.getDataFolder(), path);
            if (!f.exists()) plugin.saveResource(path, false);
            upgradeAutoBuyerButton(f);
        }
    }

    private void upgradeAutoBuyerButton(File file) {
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.load(file);
            ConfigurationSection buttons = cfg.getConfigurationSection("knops-settings");
            if (buttons == null) return;
            boolean changed = false;
            for (String key : buttons.getKeys(false)) {
                String path = key + ".action";
                if (buttons.getStringList(path).equals(Arrays.asList("[command] autobuyer", "[close]"))
                        || buttons.getStringList(path).equals(Collections.singletonList("[openautobuyer] menu"))) {
                    buttons.set(path, Collections.singletonList("[openmenu] autobuyer"));
                    buttons.set(key + ".material", "MAGMA_CREAM");
                    buttons.set(key + ".name", "&b➥ Автоскупщик");
                    changed = true;
                }
            }
            if (changed) cfg.save(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Не удалось обновить кнопку автоскупщика", e);
        }
    }

    public void reload() {
        cache.clear();
    }
}