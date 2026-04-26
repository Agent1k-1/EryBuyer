package com.erydevs.placeholders;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.utils.HexUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final EryBuyer plugin;

    public PlaceholderAPIHook(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "buyer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Agent1k";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;
        
        if (params.startsWith("money_player_")) {
            try {
                int position = Integer.parseInt(params.substring(13));
                return getTopPlayerByPosition(position);
            } catch (NumberFormatException e) {
                return "--- ---";
            }
        }

        switch (params) {
            case "autobuyer_status":
                return plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                        plugin.getConfigManager().getPlaceholderEnableAutobuyer() :
                        plugin.getConfigManager().getPlaceholderDisableAutobuyer();
            case "buyer_current_level":
                return String.valueOf(plugin.getDataBase().getPlayerData(player.getUniqueId()).getCurrentLevel());
            case "buyer_total_earned":
                return String.valueOf((long) plugin.getDataBase().getPlayerData(player.getUniqueId()).getTotalEarned());
            case "buyer_required_amount":
                double totalEarned = plugin.getDataBase().getPlayerData(player.getUniqueId()).getTotalEarned();
                int nextLevel = plugin.getDataBase().getPlayerData(player.getUniqueId()).getCurrentLevel() + 1;
                double requiredForNext = plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel);
                return String.valueOf((long) Math.max(0, requiredForNext - totalEarned));
            case "buyer_max_level":
                return String.valueOf(plugin.getLevelConfig().getMaxLevel());
            default:
                return null;
        }
    }
    
    private String getTopPlayerByPosition(int position) {
        List<Map.Entry<String, Double>> topPlayers = plugin.getDataBase().getTopPlayers(position, plugin.getConfigManager().getBuyerTopUpdateMoney());
        if (topPlayers == null || topPlayers.size() < position) {
            return "--- ---";
        }
        Map.Entry<String, Double> entry = topPlayers.get(position - 1);
        String playerName = entry.getKey();
        try {
            UUID uuid = UUID.fromString(entry.getKey());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer != null && offlinePlayer.getName() != null && !offlinePlayer.getName().isEmpty()) {
                playerName = offlinePlayer.getName();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return playerName + " " + formatDouble(entry.getValue());
    }

    private static String applyPlaceholders(String input, Player player, Entry entry, int amount, double customPrice) {
        if (input == null) return "";
        EryBuyer plugin = EryBuyer.getInstance();
        String itemName = entry != null ? stripColors(entry.name) : "";
        double priceX1 = entry != null ? entry.priceX1 : 0.0;
        double priceX64 = entry != null ? entry.priceX64 : priceX1 * 64;
        
        String autobuyerStatus = plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                plugin.getConfigManager().getPlaceholderEnableAutobuyer() :
                plugin.getConfigManager().getPlaceholderDisableAutobuyer();
        
        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(player.getUniqueId());
        double totalEarned = playerLevel.getTotalEarned();
        int currentLevel = playerLevel.getCurrentLevel();
        int nextLevel = currentLevel + 1;
        double requiredForNext = plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel);
        double remaining = Math.max(0, requiredForNext - totalEarned);
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        
        String result = input
                .replace("%item_name%", itemName)
                .replace("%prince-x1%", formatDouble(priceX1))
                .replace("%prince-x64%", formatDouble(priceX64))
                .replace("%price%", formatDouble(priceX1))
                .replace("%prince%", formatDouble(customPrice))
                .replace("%item_sell%", String.valueOf(amount))
                .replace("%autobuyer_status%", autobuyerStatus)
                .replace("%buyer_current_level%", String.valueOf(currentLevel))
                .replace("%buyer_total_earned%", String.valueOf((long) totalEarned))
                .replace("%buyer_required_amount%", String.valueOf((long) remaining))
                .replace("%max_level%", String.valueOf(maxLevel))
                .replace("%new_level%", String.valueOf(nextLevel));
        
        return HexUtils.colorize(result);
    }

    public static String apply(String input, Player player, Entry entry, int amount, double customPrice) {
        if (input == null) return "";
        if (!isAvailable()) return input;
        if (player == null) return HexUtils.colorize(input);
        
        String result = applyPlaceholders(input, player, entry, amount, customPrice);
        result = PlaceholderAPI.setPlaceholders(player, result);
        
        return result;
    }

    public static String apply(String input, Player player, Entry entry, int amount) {
        return apply(input, player, entry, amount, entry != null ? entry.priceX1 : 0.0);
    }

    public static String apply(String input, Player player) {
        return apply(input, player, null, 0, 0.0);
    }

    public static String applyLevelUp(String input, Player player, int newLevel) {
        if (input == null) return "";
        if (!isAvailable()) return input;
        if (player == null) return HexUtils.colorize(input);
        
        String withLevel = input.replace("%new_level%", String.valueOf(newLevel));
        String result = applyPlaceholders(withLevel, player, null, 0, 0.0);
        result = PlaceholderAPI.setPlaceholders(player, result);
        
        return result;
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
