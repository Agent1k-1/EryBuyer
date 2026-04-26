package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class Configuration {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public Configuration(JavaPlugin plugin) {
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

    public String getMessageAutoBuyerOn() {
        return config.getString("message.auto-buyer-on");
    }

    public String getMessageAutoBuyerOff() {
        return config.getString("message.auto-buyer-off");
    }

    public String getMessageNoItem() {
        return config.getString("message.no-item");
    }

    public String getMessageSuccessfullyBuyer() {
        return config.getString("message.successfully-buyer");
    }

    public String getMessageLevelUp() {
        return config.getString("message.level-up");
    }

    public String getSoundOpenMenu() {
        return config.getString("sound.sound_open_menu");
    }

    public String getSoundNoItem() {
        return config.getString("sound.no-item-sound");
    }

    public String getSoundAutobuyer() {
        return config.getString("sound.autobuyer-sound");
    }

    public List<String> getRegisterMenu() {
        return config.getStringList("register-menu");
    }

    public String getPlaceholderEnableAutobuyer() {
        return config.getString("placeholder.enable-autobuyer");
    }

    public String getPlaceholderDisableAutobuyer() {
        return config.getString("placeholder.disable-autobuyer");
    }

    public String getMessageNoPermission() {
        return config.getString("message.no-permission");
    }

    public List<String> getMessageLevelInfo() {
        return config.getStringList("message.level-info");
    }

    public String getBossbarText() {
        return config.getString("bossbar-settings.text");
    }

    public String getBossbarColor() {
        return config.getString("bossbar-settings.color");
    }

    public boolean isBossbarEnabled() {
        return config.getBoolean("bossbar-settings.bossbar");
    }

    public long getAutobuyerTime() {
        return config.getLong("bossbar-settings.autobuyer-time");
    }

    public String getMessageAutoBuyer() {
        return config.getString("message.auto-buyer");
    }

    public String getConfigReloadMessage() {
        return config.getString("message.config-reload");
    }

    public double getBuyerTopUpdateMoney() {
        return config.getDouble("buyer-top.update-money");
    }

    public int getBuyerTopUpdateInterval() {
        return config.getInt("buyer-top.update-interval");
    }
}
