package com.erydevs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.erydevs.config.Configs;
import com.erydevs.config.MessagesConfig;
import com.erydevs.gui.BuyerGUI;
import com.erydevs.gui.loader.MenuLoader;
import com.erydevs.commands.BuyerCommand;
import com.erydevs.commands.AutoBuyerCommand;
import com.erydevs.buyer.boosters.BoosterManager;
import com.erydevs.data.SQLite;
import com.erydevs.listeners.InventoryListener;
import com.erydevs.listeners.PlayerQuitListener;
import com.erydevs.economy.VaultAPI;
import com.erydevs.buyer.autobuyer.AutoBuyerManager;
import com.erydevs.buyer.best.BestManager;
import com.erydevs.bossbar.Bossbars;
import com.erydevs.papi.PlaceholderAPIHook;

import com.erydevs.bstats.Metrics;

public class EryBuyer extends JavaPlugin {

    private static EryBuyer instance;
    private VaultAPI vaultAPI;
    private Configs configManager;
    private MessagesConfig messagesConfig;
    private MenuLoader menuLoader;
    private BuyerGUI buyerGUI;
    private AutoBuyerManager autoBuyerManager;
    private Bossbars bossbars;
    private BoosterManager boosterManager;
    private SQLite SQLite;
    private BestManager bestManager;

    public void onEnable() {
        instance = this;

        configManager = new Configs(this);
        configManager.loadConfigs();

        messagesConfig = new MessagesConfig(this);
        messagesConfig.loadMessages();

        menuLoader = new MenuLoader(this);
        menuLoader.saveDefaults(configManager.getRegisterMenu());

        boosterManager = new BoosterManager(this);
        boosterManager.enable();
        SQLite = new SQLite(this);

        vaultAPI = new VaultAPI(this);
        Bukkit.getConsoleSender().sendMessage(vaultAPI.getStatus());
        if (!vaultAPI.isEnabled()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bestManager = new BestManager(this);
        bestManager.enable();

        buyerGUI = new BuyerGUI(this, configManager, menuLoader);
        bossbars = new Bossbars(this);
        autoBuyerManager = new AutoBuyerManager(this);

        getCommand("buyer").setExecutor(new BuyerCommand(this));
        getCommand("autobuyer").setExecutor(new AutoBuyerCommand(this));

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        startMenuRefreshTask();

        printStartupMessage();

        int pluginId = 31977;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
    }

    public void onDisable() {
        if (autoBuyerManager != null) autoBuyerManager.shutdown();
        if (bossbars != null) bossbars.shutdown();
        if (bestManager != null) bestManager.shutdown();
        if (SQLite != null) SQLite.closeConnection();
        instance = null;
        printShutdownMessage();
    }

    private void startMenuRefreshTask() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                buyerGUI.refreshOpenInventory(player);
            }
        }, 20L, 20L);
    }

    private void printStartupMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "\n" +
                "███████╗██████╗░██╗░░░██╗██████╗░██╗░░░██╗██╗░░░██╗███████╗██████╗░\n" +
                "██╔════╝██╔══██╗╚██╗░██╔╝██╔══██╗██║░░░██║╚██╗░██╔╝██╔════╝██╔══██╗\n" +
                "█████╗░░██████╔╝░╚████╔╝░██████╦╝██║░░░██║░╚████╔╝░█████╗░░██████╔╝\n" +
                "██╔══╝░░██╔══██╗░░╚██╔╝░░██╔══██╗██║░░░██║░░╚██╔╝░░██╔══╝░░██╔══██╗\n" +
                "███████╗██║░░██║░░░██║░░░██████╦╝╚██████╔╝░░░██║░░░███████╗██║░░██║\n" +
                "╚══════╝╚═╝░░╚═╝░░░╚═╝░░░╚═════╝░░╚═════╝░░░░╚═╝░░░╚══════╝╚═╝░░╚═╝");
        Bukkit.getConsoleSender().sendMessage();
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Плагин: " + ChatColor.GREEN + "включен");
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Версия плагина: " + ChatColor.YELLOW + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Ядро: " + ChatColor.YELLOW + Bukkit.getVersion());
        Bukkit.getConsoleSender().sendMessage(vaultAPI.getStatus());
    }


    private void printShutdownMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "\n" +
                "███████╗██████╗░██╗░░░██╗██████╗░██╗░░░██╗██╗░░░██╗███████╗██████╗░\n" +
                "██╔════╝██╔══██╗╚██╗░██╔╝██╔══██╗██║░░░██║╚██╗░██╔╝██╔════╝██╔══██╗\n" +
                "█████╗░░██████╔╝░╚████╔╝░██████╦╝██║░░░██║░╚████╔╝░█████╗░░██████╔╝\n" +
                "██╔══╝░░██╔══██╗░░╚██╔╝░░██╔══██╗██║░░░██║░░╚██╔╝░░██╔══╝░░██╔══██╗\n" +
                "███████╗██║░░██║░░░██║░░░██████╦╝╚██████╔╝░░░██║░░░███████╗██║░░██║\n" +
                "╚══════╝╚═╝░░╚═╝░░░╚═╝░░░╚═════╝░░╚═════╝░░░░╚═╝░░░╚══════╝╚═╝░░╚═╝");
        Bukkit.getConsoleSender().sendMessage();
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Плагин: " + ChatColor.RED + "выключен");
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Версия плагина: " + ChatColor.YELLOW + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Ядро: " + ChatColor.YELLOW + Bukkit.getVersion());
    }

    @Nullable
    public static EryBuyer getInstance() {
        return instance;
    }

    @NotNull
    public VaultAPI getEconomyManager() {
        return vaultAPI;
    }

    @NotNull
    public Configs getConfigManager() {
        return configManager;
    }

    @NotNull
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    @NotNull
    public MenuLoader getMenuRegistry() {
        return menuLoader;
    }

    @NotNull
    public BuyerGUI getBuyerGUI() {
        return buyerGUI;
    }

    @NotNull
    public AutoBuyerManager getAutoBuyerManager() {
        return autoBuyerManager;
    }

    @NotNull
    public Bossbars getBossBarManager() {
        return bossbars;
    }

    @NotNull
    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    @NotNull
    public SQLite getDataBase() {
        return SQLite;
    }

    @NotNull
    public BestManager getBestManager() {
        return bestManager;
    }
}