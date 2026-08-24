package com.erydevs.papi;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.best.BestItem;
import com.erydevs.gui.entry.Entry;
import com.erydevs.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        if (input == null) return "";
        if (player == null) return HexUtils.colorize(input);

        String result = applyOwn(input, player, entry, amount, customPrice);
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

    @NotNull
    private static String applyOwn(@NotNull String input, @NotNull Player player, @Nullable Entry entry, int amount, double customPrice) {
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
    static String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    @NotNull
    private static String stripColors(@Nullable String text) {
        if (text == null) return "";
        return ChatColor.stripColor(HexUtils.colorize(text)).trim();
    }
}
