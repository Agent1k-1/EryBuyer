package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Configuration {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMenuConfig(String name) {
        File file = new File(plugin.getDataFolder(), "menu/" + name + ".yml");
        if (!file.exists()) return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menu/menu.yml"));
        return YamlConfiguration.loadConfiguration(file);
    }
}
