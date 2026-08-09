package com.erydevs.papi;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.erydevs.EryBuyer;
import com.erydevs.buyer.best.BestItem;
import com.erydevs.buyer.boosters.BoosterManager;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.entry.Entry;
import com.erydevs.utils.HexUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final EryBuyer plugin;

    public PlaceholderAPIHook(@NotNull EryBuyer plugin) {
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

        if (params.startsWith("best_")) {
            return handleBestPlaceholder(player, params.substring(5));
        }

        if (params.startsWith("points_player_")) {
            try {
                int position = Integer.parseInt(params.substring("points_player_".length()));
                return getTopPointsByPosition(position);
            } catch (NumberFormatException e) {
                return "--- ---";
            }
        }

        BoosterManager boosterManager = plugin.getBoosterManager();
        PlayerBooster booster = plugin.getDataBase().getPlayerData(player.getUniqueId());

        switch (params) {
            case "autobuyer_status":
                return plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                        plugin.getMessagesConfig().getPlaceholderEnableAutobuyer() :
                        plugin.getMessagesConfig().getPlaceholderDisableAutobuyer();
            case "booster_lvl":
                return String.valueOf(booster.getCurrentLevel());
            case "booster_max_lvl":
                return String.valueOf(boosterManager.getConfig().getMaxLevel());
            case "factor":
                return BoosterManager.formatMultiplier(boosterManager.getMoneyMultiplier(booster));
            case "booster_multiplier":
                return BoosterManager.formatMultiplier(boosterManager.getBoosterMultiplier(booster));
            case "points":
                return String.valueOf(booster.getTotalPoints());
            case "points_next_lvl":
                if (boosterManager.isMaxLevel(booster)) return String.valueOf(booster.getTotalPoints());
                return String.valueOf(boosterManager.getPointsRequiredForNext(booster));
            case "points_remaining":
                if (boosterManager.isMaxLevel(booster)) return "0";
                return String.valueOf(Math.max(0L, boosterManager.getPointsRequiredForNext(booster) - booster.getTotalPoints()));
            case "update_bestitem":
                return plugin.getBestManager() != null
                        ? formatDuration(plugin.getBestManager().getSecondsUntilNextRotation())
                        : "00:00:00";
            default:
                return null;
        }
    }

    @NotNull
    private static String formatDuration(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @NotNull
    private String getTopPointsByPosition(int position) {
        if (position <= 0) return "--- ---";
        List<Map.Entry<String, Long>> top = plugin.getDataBase().getTopPoints();
        if (top == null || top.size() < position) return "--- ---";

        Map.Entry<String, Long> entry = top.get(position - 1);
        String playerName = entry.getKey();
        try {
            UUID uuid = UUID.fromString(entry.getKey());
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            if (off != null && off.getName() != null && !off.getName().isEmpty()) {
                playerName = off.getName();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return playerName + " " + entry.getValue();
    }

    @Nullable
    private String handleBestPlaceholder(@NotNull Player player, @NotNull String key) {
        if (plugin.getBestManager() == null) return "0";
        int idx = key.lastIndexOf('_');
        if (idx <= 0 || idx >= key.length() - 1) return null;

        String field = key.substring(0, idx);
        String slotStr = key.substring(idx + 1);
        int slot;
        try {
            slot = Integer.parseInt(slotStr);
        } catch (NumberFormatException e) {
            return null;
        }

        BestItem item = plugin.getBestManager().getActiveBySlot(slot);
        if (item == null) return "0";

        int sold = plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName());
        switch (field) {
            case "custom_price_x1":
                return formatDouble(item.getCustomPrice());
            case "custom_price_x64":
                return formatDouble(item.getCustomPrice64());
            case "default_price":
                return formatDouble(item.getDefaultPrice());
            case "default_price_64":
                return formatDouble(item.getDefaultPrice64());
            case "limit":
                return String.valueOf(item.getLimit());
            case "limit_now":
                return String.valueOf(sold);
            case "material":
                return item.getMaterialName();
            default:
                return null;
        }
    }

    @NotNull
    private static String applyPlaceholders(@Nullable String input, @NotNull Player player, @Nullable Entry entry, int amount, double customPrice) {
        if (input == null) return "";
        EryBuyer plugin = EryBuyer.getInstance();
        String itemName = entry != null ? stripColors(entry.name) : "";
        double priceX1 = entry != null ? entry.priceX1 : 0.0;
        double priceX64 = entry != null ? entry.priceX64 : priceX1 * 64;

        String autobuyerStatus = plugin.getAutoBuyerManager().isAutobuyerEnabled(player) ?
                plugin.getMessagesConfig().getPlaceholderEnableAutobuyer() :
                plugin.getMessagesConfig().getPlaceholderDisableAutobuyer();

        String result = input
                .replace("%item_name%", itemName)
                .replace("%prince-x1%", formatDouble(priceX1))
                .replace("%prince-x64%", formatDouble(priceX64))
                .replace("%price%", formatDouble(priceX1))
                .replace("%prince%", formatDouble(customPrice))
                .replace("%item_sell%", String.valueOf(amount))
                .replace("%autobuyer_status%", autobuyerStatus);

        return HexUtils.colorize(result);
    }

    @NotNull
    public static String apply(@Nullable String input, @Nullable Player player, @Nullable Entry entry, int amount, double customPrice) {
        if (input == null) return "";
        if (!isAvailable()) return input;
        if (player == null) return HexUtils.colorize(input);

        String result = applyPlaceholders(input, player, entry, amount, customPrice);
        result = PlaceholderAPI.setPlaceholders(player, result);

        return result;
    }

    @NotNull
    public static String apply(@Nullable String input, @Nullable Player player, @Nullable Entry entry, int amount) {
        return apply(input, player, entry, amount, entry != null ? entry.priceX1 : 0.0);
    }

    @NotNull
    public static String apply(@Nullable String input, @Nullable Player player) {
        return apply(input, player, null, 0, 0.0);
    }

    @NotNull
    public static String applyBest(@Nullable String input, @NotNull BestItem item, int soldNow) {
        if (input == null) return "";
        int remaining = Math.max(0, item.getLimit() - soldNow);
        return input
                .replace("%custom-item-price-x1%", formatDouble(item.getCustomPrice()))
                .replace("%custom-item-price-x64%", formatDouble(item.getCustomPrice64()))
                .replace("%default-price%", formatDouble(item.getDefaultPrice()))
                .replace("%default-price-64%", formatDouble(item.getDefaultPrice64()))
                .replace("%buyer_limit_max%", String.valueOf(item.getLimit()))
                .replace("%buyer_limit_now%", String.valueOf(soldNow))
                .replace("%buyer_limit_remaining%", String.valueOf(remaining))
                .replace("%best_material%", item.getMaterialName())
                .replace("%item_name%", item.getMaterialName());
    }

    @NotNull
    public static List<String> applyList(@NotNull List<String> list, @Nullable Player player, @Nullable Entry entry, int amount) {
        return list.stream().map(l -> apply(l, player, entry, amount)).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> applyList(@NotNull List<String> list, @Nullable Player player) {
        return applyList(list, player, null, 0);
    }

    private static double getBalance(@NotNull Player player) {
        EryBuyer plugin = EryBuyer.getInstance();
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ == null) return 0.0;
        return econ.getBalance(player);
    }

    @NotNull
    private static String formatDouble(double d) {
        return String.format("%.2f", d);
    }

    @NotNull
    private static String stripColors(@Nullable String s) {
        if (s == null) return "";
        return ChatColor.stripColor(HexUtils.colorize(s)).trim();
    }
}
