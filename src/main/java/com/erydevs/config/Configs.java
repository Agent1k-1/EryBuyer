package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Configs {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public Configs(@NotNull JavaPlugin plugin) {
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

    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    @NotNull
    public String getSoundOpenMenu() {
        return config.getString("sound.sound_open_menu.sound");
    }

    public float getSoundOpenMenuPitch() {
        return (float) config.getDouble("sound.sound_open_menu.pitch");
    }

    public float getSoundOpenMenuVolume() {
        return (float) config.getDouble("sound.sound_open_menu.volume");
    }

    public boolean isSoundOpenMenuEnabled() {
        return config.getBoolean("sound.sound_open_menu.enabled");
    }

    @NotNull
    public String getSoundNoItem() {
        return config.getString("sound.no-item-sound.sound");
    }

    public float getSoundNoItemPitch() {
        return (float) config.getDouble("sound.no-item-sound.pitch");
    }

    public float getSoundNoItemVolume() {
        return (float) config.getDouble("sound.no-item-sound.volume");
    }

    public boolean isSoundNoItemEnabled() {
        return config.getBoolean("sound.no-item-sound.enabled");
    }

    @NotNull
    public String getSoundExchange() {
        return config.getString("sound.exchange-sound.sound");
    }

    public float getSoundExchangePitch() {
        return (float) config.getDouble("sound.exchange-sound.pitch");
    }

    public float getSoundExchangeVolume() {
        return (float) config.getDouble("sound.exchange-sound.volume");
    }

    public boolean isSoundExchangeEnabled() {
        return config.getBoolean("sound.exchange-sound.enabled");
    }

    @NotNull
    public String getSoundAutobuyer() {
        return config.getString("sound.autobuyer-sound.sound");
    }

    public float getSoundAutobuyerPitch() {
        return (float) config.getDouble("sound.autobuyer-sound.pitch");
    }

    public float getSoundAutobuyerVolume() {
        return (float) config.getDouble("sound.autobuyer-sound.volume");
    }

    public boolean isSoundAutobuyerEnabled() {
        return config.getBoolean("sound.autobuyer-sound.enabled");
    }

    @NotNull
    public String getDatabaseFileName() {
        String fileName = config.getString("database.file");
        return fileName != null ? fileName : "playerdata.db";
    }

    public boolean isBossbarEnabled() {
        return config.getBoolean("bossbar-settings.bossbar");
    }

    @NotNull
    public String getBossbarText() {
        return config.getString("bossbar-settings.text");
    }

    @NotNull
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

    @NotNull
    public List<String> getRegisterMenu() {
        return config.getStringList("register-menu");
    }
}
