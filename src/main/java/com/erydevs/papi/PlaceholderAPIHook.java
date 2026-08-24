package com.erydevs.papi;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.best.BestItem;
import com.erydevs.buyer.boosters.BoosterManager;
import com.erydevs.buyer.boosters.PlayerBooster;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private static final String BEST_PREFIX = "best_";
    private static final String TOP_PREFIX = "points_player_";
    private static final String EMPTY_TOP = "--- ---";

    private final EryBuyer plugin;

    public PlaceholderAPIHook(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
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

        if (params.startsWith(BEST_PREFIX)) {
            return handleBestPlaceholder(player, params.substring(BEST_PREFIX.length()));
        }

        if (params.startsWith(TOP_PREFIX)) {
            try {
                return getTopPointsByPosition(Integer.parseInt(params.substring(TOP_PREFIX.length())));
            } catch (NumberFormatException e) {
                return EMPTY_TOP;
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
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }

    @NotNull
    private String getTopPointsByPosition(int position) {
        if (position <= 0) return EMPTY_TOP;

        List<Map.Entry<String, Long>> top = plugin.getDataBase().getTopPoints();
        if (top == null || top.size() < position) return EMPTY_TOP;

        Map.Entry<String, Long> entry = top.get(position - 1);
        String playerName = entry.getKey();
        try {
            OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
            if (off.getName() != null && !off.getName().isEmpty()) {
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

        int slot;
        try {
            slot = Integer.parseInt(key.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }

        BestItem item = plugin.getBestManager().getActiveBySlot(slot);
        if (item == null) return "0";

        switch (key.substring(0, idx)) {
            case "custom_price_x1":
                return Placeholders.formatDouble(item.getCustomPrice());
            case "custom_price_x64":
                return Placeholders.formatDouble(item.getCustomPrice64());
            case "default_price":
                return Placeholders.formatDouble(item.getDefaultPrice());
            case "default_price_64":
                return Placeholders.formatDouble(item.getDefaultPrice64());
            case "limit":
                return String.valueOf(item.getLimit());
            case "limit_now":
                return String.valueOf(plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName()));
            case "material":
                return item.getMaterialName();
            default:
                return null;
        }
    }
}
