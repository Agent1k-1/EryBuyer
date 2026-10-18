package com.erydevs.papi;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.best.BestItem;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.entry.Entry;
import com.erydevs.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class Placeholders {

    private Placeholders() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @NotNull
    public static String apply(@Nullable String input, @Nullable Player player, @Nullable Entry entry, int amount, double customPrice) {
        return apply(input, player, entry, amount, customPrice, entry != null ? entry.material : null);
    }

    private static String apply(String input, Player player, Entry entry, int amount, double customPrice, Material material) {
        if (input == null) return "";
        if (player == null) return HexUtils.colorize(input);

        String result = applyOwn(input, player, entry, amount, customPrice, material);
        return isAvailable() ? PlaceholderApiBridge.apply(player, result) : result;
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
                .replace("%custom-item-factor%", formatDouble(item.getDefaultPrice() > 0 ? item.getCustomPrice() / item.getDefaultPrice() : 1.0))
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
    public static String applyBest(@Nullable String input, @NotNull Player player, @NotNull BestItem item, int soldNow) {
        return apply(applyBest(input, item, soldNow), player, null, 0, 0.0, item.getMaterial());
    }

    @NotNull
    public static List<String> applyList(@NotNull List<String> list, @Nullable Player player, @Nullable Entry entry, int amount) {
        return list.stream().map(l -> apply(l, player, entry, amount)).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> applyList(@NotNull List<String> list, @Nullable Player player) {
        return applyList(list, player, null, 0);
    }

    @NotNull
    private static String applyOwn(@NotNull String input, @NotNull Player player, @Nullable Entry entry, int amount, double customPrice, @Nullable Material material) {
        EryBuyer plugin = EryBuyer.getInstance();
        String itemName = entry != null ? stripColors(entry.messageName != null ? entry.messageName : entry.name) : "";
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

        if (result.contains("%buyer_booster_status%") || result.contains("%buyer_factor%") || result.contains("%buyer_points%")) {
            PlayerBooster booster = plugin.getDataBase().getPlayerData(player.getUniqueId());
            result = result.replace("%buyer_booster_status%", getBoosterStatus(plugin, player, booster))
                    .replace("%buyer_points%", String.valueOf(booster.getTotalPoints()));
            if (result.contains("%buyer_factor%")) {
                result = result.replace("%buyer_factor%", Double.toString(plugin.getBoosterManager().getMoneyMultiplier(booster)));
            }
        }
        if (result.contains("%buyer_booster_timer%")) {
            result = result.replace("%buyer_booster_timer%", getBoosterTimer(plugin, player.getUniqueId()));
        }
        if (result.contains("%buyer_booster_name%")) {
            String name = plugin.getShopManager() != null
                    ? plugin.getShopManager().getPurchasedBoosterName(player.getUniqueId()) : null;
            result = result.replace("%buyer_booster_name%", name != null ? name : "");
        }
        if (result.contains("%autobuyer_item_status%")) {
            String status = "";
            if (material != null) {
                status = !plugin.getAutoBuyerSettings().isLoaded(player.getUniqueId()) ? "&7Загрузка..."
                        : plugin.getAutoBuyerSettings().isItemEnabled(player.getUniqueId(), material)
                        ? plugin.getMessagesConfig().getPlaceholderEnableItemAutobuyer()
                        : plugin.getMessagesConfig().getPlaceholderDisableItemAutobuyer();
            }
            result = result.replace("%autobuyer_item_status%", status);
        }
        return HexUtils.colorize(result);
    }

    @NotNull
    public static String getBoosterTimer(@NotNull EryBuyer plugin, @NotNull java.util.UUID uuid) {
        long seconds = plugin.getShopManager() != null ? plugin.getShopManager().getRemainingBoosterSeconds(uuid) : 0;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    @NotNull
    public static String getBoosterStatus(@NotNull EryBuyer plugin, @NotNull Player player, @NotNull PlayerBooster booster) {
        String name = plugin.getShopManager() != null
                ? plugin.getShopManager().getPurchasedBoosterName(player.getUniqueId()) : null;
        return name != null ? HexUtils.colorize(name) : getBoosterStatus(booster);
    }

    @NotNull
    public static String getBoosterStatus(@NotNull PlayerBooster booster) {
        return HexUtils.colorize(booster.getCurrentLevel() <= 0 ? "&aНет бустера"
                : "&bБустер " + booster.getCurrentLevel() + " ур.");
    }

    @NotNull
    static String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    @NotNull
    private static String stripColors(@Nullable String text) {
        if (text == null) return "";
        return ChatColor.stripColor(HexUtils.colorize(text)).trim();
    }
}
