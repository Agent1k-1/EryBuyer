package com.erydevs.economy;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultAPI {

    private final JavaPlugin plugin;
    private Economy economy;
    private boolean enabled;

    public VaultAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabled = setup();
    }

    private boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStatus() {
        if (enabled) {
            return ChatColor.WHITE + "Vault: " + ChatColor.GREEN + "подключен";
        } else {
            return ChatColor.WHITE + "Vault: " + ChatColor.RED + "не удалось подключить";
        }
    }

    public Economy getEconomy() {
        return economy;
    }
}