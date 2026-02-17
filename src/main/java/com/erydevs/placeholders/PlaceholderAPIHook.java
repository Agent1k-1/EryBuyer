package com.erydevs.placeholders;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.utils.HexUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class BuyerPlaceholder extends PlaceholderExpansion {

    private final EryBuyer plugin;

    public BuyerPlaceholder(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "erybuyer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EryDev";
    }

    @Override
    public @NotNull String getVersion() {
        return "v2";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;

        switch (params) {
            case "balance":
                return formatDouble(getBalance(player));
            case "autobuyer_status":
                return plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                        plugin.getConfigManager().getConfig().getString("placeholder.enable-autobuyer") :
                        plugin.getConfigManager().getConfig().getString("placeholder.disable-autobuyer");
            case "buyer_current_level":
                return String.valueOf(plugin.getDataBase().getPlayerData(player.getUniqueId()).getCurrentLevel());
            case "buyer_total_earned":
                return String.valueOf((long) plugin.getDataBase().getPlayerData(player.getUniqueId()).getTotalEarned());
            case "buyer_required_amount":
                double totalEarned = plugin.getDataBase().getPlayerData(player.getUniqueId()).getTotalEarned();
                int nextLevel = plugin.getDataBase().getPlayerData(player.getUniqueId()).getCurrentLevel() + 1;
                double requiredForNext = plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel);
                return String.valueOf((long) Math.max(0, requiredForNext - totalEarned));
            default:
                return null;
        }
    }

    public static String apply(String input, Player player, Entry entry, int amount) {
        if (input == null) return "";
        
        EryBuyer plugin = EryBuyer.getInstance();
        String itemName = entry != null ? stripColors(entry.name) : "";
        double priceX1 = entry != null ? entry.priceX1 : 0.0;
        double priceX64 = entry != null ? entry.priceX64 : priceX1 * 64;
        
        String autobuyerStatus = plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                plugin.getConfigManager().getConfig().getString("placeholder.enable-autobuyer") :
                plugin.getConfigManager().getConfig().getString("placeholder.disable-autobuyer");
        
        com.erydevs.levels.PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(player.getUniqueId());
        double totalEarned = playerLevel.getTotalEarned();
        int currentLevel = playerLevel.getCurrentLevel();
        int nextLevel = currentLevel + 1;
        double requiredForNext = plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel);
        double remaining = Math.max(0, requiredForNext - totalEarned);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%item_name%", itemName);
        placeholders.put("%prince-x1%", formatDouble(priceX1));
        placeholders.put("%prince-x64%", formatDouble(priceX64));
        placeholders.put("%price%", formatDouble(priceX1));
        placeholders.put("%item_sell%", String.valueOf(amount));
        placeholders.put("%balance%", formatDouble(getBalance(player)));
        placeholders.put("%autobuyer_status%", autobuyerStatus);
        placeholders.put("%buyer_current_level%", String.valueOf(currentLevel));
        placeholders.put("%buyer_total_earned%", String.valueOf((long) totalEarned));
        placeholders.put("%buyer_required_amount%", String.valueOf((long) remaining));
        
        String s = input;
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }
        
        return HexUtils.colorize(s);
    }

    public static String apply(String input, Player player) {
        return apply(input, player, null, 0);
    }

    public static List<String> applyList(List<String> list, Player player, Entry entry, int amount) {
        return list.stream().map(l -> apply(l, player, entry, amount)).collect(Collectors.toList());
    }

    public static List<String> applyList(List<String> list, Player player) {
        return applyList(list, player, null, 0);
    }

    private static double getBalance(Player player) {
        EryBuyer plugin = EryBuyer.getInstance();
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ == null) return 0.0;
        return econ.getBalance(player);
    }

    private static String formatDouble(double d) {
        return String.format("%.2f", d);
    }

    private static String stripColors(String s) {
        if (s == null) return "";
        return ChatColor.stripColor(HexUtils.colorize(s)).trim();
    }
}
