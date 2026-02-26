package com.erydevs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.erydevs.config.Configuration;
import com.erydevs.gui.BuyerGUI;
import com.erydevs.commands.BuyerCommand;
import com.erydevs.commands.AutoBuyerCommand;
import com.erydevs.commands.LevelCommand;
import com.erydevs.levels.LevelConfig;
import com.erydevs.data.DataBase;
import com.erydevs.listeners.InventoryListener;
import com.erydevs.listeners.PlayerPickupListener;
import com.erydevs.listeners.PlayerQuitListener;
import com.erydevs.listeners.PlayerInventoryListener;
import com.erydevs.economy.EconomyManager;
import com.erydevs.autobuyer.AutoBuyerManager;
import com.erydevs.bossbar.BossBarManager;
import com.erydevs.placeholders.PlaceholderAPIHook;

import java.io.File;
import java.util.List;

public class EryBuyer extends JavaPlugin {

    private static EryBuyer instance;
    private EconomyManager economyManager;
    private Configuration configManager;
    private BuyerGUI buyerGUI;
    private AutoBuyerManager autoBuyerManager;
    private BossBarManager bossBarManager;
    private LevelConfig levelConfig;
    private DataBase dataBase;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        File menuDir = new File(getDataFolder(), "menu");
        if (!menuDir.exists()) menuDir.mkdirs();
        List<String> register = getConfig().getStringList("register-menu");
        for (String path : register) {
            if (path == null || path.trim().isEmpty()) continue;
            File f = new File(getDataFolder(), path);
            if (!f.exists()) {
                saveResource(path, false);
            }
        }

        configManager = new Configuration(this);
        levelConfig = new LevelConfig(this);
        dataBase = new DataBase(getDataFolder());
        economyManager = new EconomyManager(this);
        if (!economyManager.isEnabled()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        buyerGUI = new BuyerGUI(this, configManager);
        bossBarManager = new BossBarManager(this);
        autoBuyerManager = new AutoBuyerManager(this);

        if (getCommand("buyer") != null)
            getCommand("buyer").setExecutor(new BuyerCommand(this));
        if (getCommand("autobuyer") != null)
            getCommand("autobuyer").setExecutor(new AutoBuyerCommand(this));
        if (getCommand("level") != null)
            getCommand("level").setExecutor(new LevelCommand(this));

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        printStartupMessage();
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
    }

    public void onDisable() {
        bossBarManager.removeAllBossBars();
        dataBase.closeConnection();
        instance = null;
        printShutdownMessage();
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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public Configuration getConfigManager() {
        return configManager;
    }

    public BuyerGUI getBuyerGUI() {
        return buyerGUI;
    }

    public AutoBuyerManager getAutoBuyerManager() {
        return autoBuyerManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public LevelConfig getLevelConfig() {
        return levelConfig;
    }

    public DataBase getDataBase() {
        return dataBase;
    }
}