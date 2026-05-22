package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class Configs {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public Configs(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<String> getMessageNoItem() {
        return config.getStringList("message.error-no-item");
    }

    public List<String> getMessageSuccessfullyBuyer() {
        return config.getStringList("message.successfully-buyer");
    }

    public List<String> getMessageAutoBuyer() {
        return config.getStringList("message.autobuyer-use");
    }

    public List<String> getMessageAutoBuyerStatus() {
        return config.getStringList("message.autobuyer-status");
    }

    public List<String> getMessageNoPermission() {
        return config.getStringList("message.error-permission");
    }

    public List<String> getMessageLevelUp() {
        return config.getStringList("message.new-level");
    }

    public List<String> getConfigReloadMessage() {
        return config.getStringList("message.config-reload");
    }

    public String getPlaceholderEnableAutobuyer() {
        return config.getString("placeholder.enable-autobuyer");
    }

    public String getPlaceholderDisableAutobuyer() {
        return config.getString("placeholder.disable-autobuyer");
    }

    public String getSoundOpenMenu() {
        return config.getString("sound.sound_open_menu.sound");
    }

    public boolean isSoundOpenMenuEnabled() {
        return config.getBoolean("sound.sound_open_menu.enabled");
    }

    public String getSoundNoItem() {
        return config.getString("sound.no-item-sound.sound");
    }

    public boolean isSoundNoItemEnabled() {
        return config.getBoolean("sound.no-item-sound.enabled");
    }

    public String getSoundAutobuyer() {
        return config.getString("sound.autobuyer-sound.sound");
    }

    public boolean isSoundAutobuyerEnabled() {
        return config.getBoolean("sound.autobuyer-sound.enabled");
    }

    public String getDatabaseFileName() {
        String fileName = config.getString("database.file");
        return fileName != null ? fileName : "playerdata.db";
    }

    public boolean isBossbarEnabled() {
        return config.getBoolean("bossbar-settings.bossbar");
    }

    public String getBossbarText() {
        return config.getString("bossbar-settings.text");
    }

    public String getBossbarColor() {
        return config.getString("bossbar-settings.color");
    }

    public long getAutobuyerTime() {
        return config.getLong("bossbar-settings.autobuyer-time");
    }

    public double getBuyerTopUpdateMoney() {
        return config.getDouble("buyer-top.update-money");
    }

    public int getBuyerTopUpdateInterval() {
        return config.getInt("buyer-top.update-interval");
    }

    public List<String> getRegisterMenu() {
        return config.getStringList("register-menu");
    }
}
