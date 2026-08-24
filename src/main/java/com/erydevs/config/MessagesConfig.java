package com.erydevs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class MessagesConfig {

    private static final String FILE_NAME = "messages.yml";

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration messages;

    public MessagesConfig(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reloadMessages() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), FILE_NAME);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    @NotNull
    public FileConfiguration getMessages() {
        return messages;
    }

    @NotNull
    public List<String> getMessageNoItem() {
        return messages.getStringList("message.error-no-item");
    }

    @NotNull
    public List<String> getMessageSuccessfullyBuyer() {
        return messages.getStringList("message.successfully-buyer");
    }

    @NotNull
    public List<String> getMessageAutoBuyer() {
        return messages.getStringList("message.autobuyer-use");
    }

    @NotNull
    public List<String> getMessageAutoBuyerStatus() {
        return messages.getStringList("message.autobuyer-status");
    }

    @NotNull
    public List<String> getMessageNoPermission() {
        return messages.getStringList("message.error-permission");
    }

    @NotNull
    public List<String> getMessageNewBooster() {
        return messages.getStringList("message.new-booster");
    }

    @NotNull
    public List<String> getMessageLessThan64Items() {
        return messages.getStringList("message.less-than-64-items");
    }

    @NotNull
    public List<String> getMessageLimitCompleted() {
        return messages.getStringList("message.limit-completed");
    }

    @NotNull
    public List<String> getMessageUpdateBestItem() {
        return messages.getStringList("message.update-best-item");
    }

    @NotNull
    public List<String> getMessageAdminBuyer() {
        return messages.getStringList("message.admin-buyer");
    }

    @NotNull
    public List<String> getMessageConfigReload() {
        return messages.getStringList("message.config-reload");
    }

    @NotNull
    public String getPlaceholderEnableAutobuyer() {
        return messages.getString("placeholder.enable-autobuyer");
    }

    @NotNull
    public String getPlaceholderDisableAutobuyer() {
        return messages.getString("placeholder.disable-autobuyer");
    }
}
