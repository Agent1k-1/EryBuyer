package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
}
