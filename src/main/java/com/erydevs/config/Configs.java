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

    public String getMessageNoItem() {
        return config.getString("message.error-no-item");
    }

    public String getMessageSuccessfullyBuyer() {
        return config.getString("message.successfully-buyer");
    }

    public String getMessageAutoBuyer() {
        return config.getString("message.autobuyer-use");
    }

    public String getMessageAutoBuyerOn() {
        return config.getString("message.autobuyer-on");
    }

    public String getMessageAutoBuyerOff() {
        return config.getString("message.autobuyer-off");
    }

    public String getMessageNoPermission() {
        return config.getString("message.error-permission");
    }

    public String getMessageLevelUp() {
        return config.getString("message.new-level");
    }

    public String getConfigReloadMessage() {
        return config.getString("message.config-reload");
    }

    public List<String> getMessageLevelInfo() {
        return config.getStringList("message.level-info");
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
        String fileName = config.getString("database.sqlite.file");
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