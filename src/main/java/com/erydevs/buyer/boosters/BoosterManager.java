package com.erydevs.buyer.boosters;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.papi.PlaceholderAPIHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class BoosterManager {

    private final EryBuyer plugin;
    private final BoosterConfig boosterConfig;

    public BoosterManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.boosterConfig = new BoosterConfig(plugin);
    }

    public void enable() {
        boosterConfig.load();
    }

    public void reload() {
        boosterConfig.reload();
    }

    @NotNull
    public BoosterConfig getConfig() {
        return boosterConfig;
    }

    public double getMoneyMultiplier(@NotNull PlayerBooster booster) {
        if (booster.getCurrentLevel() <= 0) return 1.0;
        return boosterConfig.getMoneyMultiplier(booster.getCurrentLevel());
    }

    public double getBoosterMultiplier(@NotNull PlayerBooster booster) {
        if (booster.getCurrentLevel() <= 0) return 1.0;
        return boosterConfig.getBoosterMultiplier(booster.getCurrentLevel());
    }

    public int getPointsRequiredForNext(@NotNull PlayerBooster booster) {
        int nextLevel = booster.getCurrentLevel() + 1;
        return boosterConfig.getPointsForLevel(nextLevel);
    }

    public boolean isMaxLevel(@NotNull PlayerBooster booster) {
        return booster.getCurrentLevel() >= boosterConfig.getMaxLevel();
    }

    public void addPointsAndCheckLevelUp(@NotNull Player player, @NotNull PlayerBooster booster, long amount) {
        if (amount <= 0) return;
        booster.addPoints(amount);
        checkLevelUp(player, booster);
    }

    private void checkLevelUp(@NotNull Player player, @NotNull PlayerBooster booster) {
        int maxLevel = boosterConfig.getMaxLevel();
        boolean leveled = false;

        while (booster.getCurrentLevel() < maxLevel) {
            int nextLevel = booster.getCurrentLevel() + 1;
            int required = boosterConfig.getPointsForLevel(nextLevel);
            if (required <= 0 || booster.getTotalPoints() < required) break;

            booster.setCurrentLevel(nextLevel);
            leveled = true;
            broadcastNewBooster(player, nextLevel);
        }

        if (leveled) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> plugin.getDataBase().save(booster));
        }
    }

    private void broadcastNewBooster(@NotNull Player player, int newLevel) {
        double booster = boosterConfig.getBoosterMultiplier(newLevel);
        List<String> lines = plugin.getMessagesConfig().getMessageNewBooster().stream()
                .map(line -> line.replace("%buyer_new_booster%", formatMultiplier(booster)))
                .map(line -> PlaceholderAPIHook.apply(line, player))
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, player, lines);
    }

    @NotNull
    public static String formatMultiplier(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);
        return String.format("%.2f", value);
    }
}
