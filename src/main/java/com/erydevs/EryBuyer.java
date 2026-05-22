package com.erydevs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.erydevs.config.Configs;
import com.erydevs.gui.BuyerGUI;
import com.erydevs.gui.menu.MenuRegistry;
import com.erydevs.commands.BuyerCommand;
import com.erydevs.commands.AutoBuyerCommand;
import com.erydevs.commands.EryBuyerCommand;
import com.erydevs.levels.LevelConfig;
import com.erydevs.data.SQLite;
import com.erydevs.listeners.InventoryListener;
import com.erydevs.listeners.PlayerQuitListener;
import com.erydevs.economy.VaultAPI;
import com.erydevs.autobuyer.AutoBuyerManager;
import com.erydevs.bossbar.Bossbars;
import com.erydevs.papi.PlaceholderAPIHook;

import java.io.File;
import java.util.List;

public class EryBuyer extends JavaPlugin {

    private static EryBuyer instance;
    private VaultAPI vaultAPI;
    private Configs configManager;
    private MenuRegistry menuRegistry;
    private BuyerGUI buyerGUI;
    private AutoBuyerManager autoBuyerManager;
    private Bossbars bossbars;
    private LevelConfig levelConfig;
    private SQLite SQLite;

    public void onEnable() {
        instance = this;

        configManager = new Configs(this);
        configManager.loadConfigs();

        File menuDir = new File(getDataFolder(), "menu");
        if (!menuDir.exists()) menuDir.mkdirs();
        List<String> register = configManager.getRegisterMenu();
        for (String path : register) {
            if (path == null || path.trim().isEmpty()) continue;
            File f = new File(getDataFolder(), path);
            if (!f.exists()) {
                saveResource(path, false);
            }
        }

        menuRegistry = new MenuRegistry(this);
        levelConfig = new LevelConfig(this);
        SQLite = new SQLite(getDataFolder(), configManager.getDatabaseFileName());

        if (SQLite.isConnected()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EryBuyer] База данных SQLite подключена");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EryBuyer] База данных SQLite не подключена");
        }

        vaultAPI = new VaultAPI(this);
        Bukkit.getConsoleSender().sendMessage(vaultAPI.getStatus());
        if (!vaultAPI.isEnabled()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        buyerGUI = new BuyerGUI(this, configManager, menuRegistry);
        bossbars = new Bossbars(this);
        autoBuyerManager = new AutoBuyerManager(this);

        if (getCommand("buyer") != null)
            getCommand("buyer").setExecutor(new BuyerCommand(this));
        if (getCommand("autobuyer") != null)
            getCommand("autobuyer").setExecutor(new AutoBuyerCommand(this));

        EryBuyerCommand eryBuyerCommand = new EryBuyerCommand(this);
        if (getCommand("erybuyer") != null) {
            getCommand("erybuyer").setExecutor(eryBuyerCommand);
            getCommand("erybuyer").setTabCompleter(eryBuyerCommand);
        }

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        printStartupMessage();
    }

    public void onDisable() {
        if (autoBuyerManager != null) autoBuyerManager.shutdown();
        if (bossbars != null) bossbars.shutdown();
        if (SQLite != null) SQLite.closeConnection();
        instance = null;
        printShutdownMessage();
    }

    public void reload() {
        configManager.reloadConfig();
        levelConfig.reloadLevels();
        buyerGUI.reloadMenus();
        bossbars.reload();
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
        if (SQLite != null && SQLite.isConnected()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "База данных: " + ChatColor.GREEN + "SQLite подключена");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "База данных: " + ChatColor.RED + "SQLite не подключена");
        }
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

    public static EryBuyer getInstance() {
        return instance;
    }

    public VaultAPI getEconomyManager() {
        return vaultAPI;
    }

    public Configs getConfigManager() {
        return configManager;
    }

    public MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }

    public BuyerGUI getBuyerGUI() {
        return buyerGUI;
    }

    public AutoBuyerManager getAutoBuyerManager() {
        return autoBuyerManager;
    }

    public Bossbars getBossBarManager() {
        return bossbars;
    }

    public LevelConfig getLevelConfig() {
        return levelConfig;
    }

    public SQLite getDataBase() {
        return SQLite;
    }
}
