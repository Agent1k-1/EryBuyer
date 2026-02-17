package com.erydevs.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {

    private final JavaPlugin plugin;
    private Economy economy;
    private boolean enabled;

    public EconomyManager(JavaPlugin plugin) {
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

    public Economy getEconomy() {
        return economy;
    }

    public double getBalance(OfflinePlayer player) {
        if (!enabled) return 0.0;
        return economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (!enabled) return false;
        return economy.has(player, amount);
    }

    public EconomyResponse deposit(Player player, double amount) {
        if (!enabled) return new EconomyResponse(false, 0.0, null);
        return new EconomyResponse(true, economy.depositPlayer(player, amount).balance, null);
    }

    public EconomyResponse withdraw(Player player, double amount) {
        if (!enabled) return new EconomyResponse(false, 0.0, null);
        return new EconomyResponse(true, economy.withdrawPlayer(player, amount).balance, null);
    }

    public static class EconomyResponse {
        public final boolean success;
        public final double balance;
        public final String error;
        public EconomyResponse(boolean success, double balance, String error) {
            this.success = success;
            this.balance = balance;
            this.error = error;
        }
    }
}