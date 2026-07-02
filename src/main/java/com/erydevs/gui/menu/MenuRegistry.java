package com.erydevs.gui.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MenuRegistry {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> cache = new ConcurrentHashMap<>();

    public MenuRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public FileConfiguration getMenuConfig(String name) {
        FileConfiguration cached = cache.get(name);
        if (cached != null) return cached;
        return cache.computeIfAbsent(name, this::load);
    }

    private FileConfiguration load(String name) {
        File file = new File(plugin.getDataFolder(), "menu/" + name + ".yml");
        if (!file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        cache.clear();
    }
}