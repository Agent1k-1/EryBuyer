package com.erydevs.gui.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class MenuRegistry {

    private final JavaPlugin plugin;

    public MenuRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public FileConfiguration getMenuConfig(String name) {
        File file = new File(plugin.getDataFolder(), "menu/" + name + ".yml");
        if (!file.exists()) return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menu/menu.yml"));
        return YamlConfiguration.loadConfiguration(file);
    }
}
