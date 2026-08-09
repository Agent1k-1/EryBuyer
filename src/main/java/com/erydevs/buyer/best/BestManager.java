package com.erydevs.buyer.best;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.click.ClickType;
import com.erydevs.gui.entry.Entry;
import com.erydevs.papi.PlaceholderAPIHook;
import com.erydevs.utils.HexUtils;
import com.erydevs.utils.inventory.InventoryUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class BestManager {

    private final EryBuyer plugin;
    private final BestConfig bestConfig;
    private final Random random = new Random();

    private final Map<Integer, BestItem> activeItems = new HashMap<>();
    private final List<String> poolOrder = new ArrayList<>();
    private int rotationOffset = 0;
    private long nextRotationAt = 0L;
    private boolean firstRotation = true;

    private BukkitTask rotationTask;

    public BestManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.bestConfig = new BestConfig(plugin);
    }

    public void enable() {
        if (!isEnabledInConfig()) return;

        bestConfig.load();
        rebuildPoolOrder();
        rotate();
        startRotationTask();
    }

    public void shutdown() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
        activeItems.clear();
    }

    public void reload() {
        shutdown();
        enable();
    }

    public boolean isEnabledInConfig() {
        return plugin.getConfigManager().isBestMenuEnabled();
    }

    @NotNull
    public BestConfig getBestConfig() {
        return bestConfig;
    }

    @NotNull
    public Map<Integer, BestItem> getActiveItems() {
        return activeItems;
    }

    @Nullable
    public BestItem getActiveBySlot(int slot) {
        return activeItems.get(slot);
    }

    @Nullable
    public BestItem getActiveByMaterial(@NotNull Material material) {
        for (BestItem item : activeItems.values()) {
            if (item.getMaterial() == material) return item;
        }
        return null;
    }

    private void rebuildPoolOrder() {
        poolOrder.clear();
        poolOrder.addAll(bestConfig.getPool().keySet());
    }

    private void startRotationTask() {
        long intervalTicks = Math.max(20L, plugin.getConfigManager().getUpdateCustomItems() * 20L);
        rotationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::rotate, intervalTicks, intervalTicks);
    }

    public long getNextRotationAt() {
        return nextRotationAt;
    }

    public long getSecondsUntilNextRotation() {
        long now = System.currentTimeMillis();
        return Math.max(0L, (nextRotationAt - now) / 1000L);
    }

    public void rotate() {
        activeItems.clear();
        plugin.getDataBase().resetAllLimits();
        nextRotationAt = System.currentTimeMillis() + plugin.getConfigManager().getUpdateCustomItems() * 1000L;

        if (poolOrder.isEmpty() || bestConfig.getSlots().isEmpty()) return;

        List<Integer> slots = bestConfig.getSlots();
        int slotCount = slots.size();
        int poolSize = poolOrder.size();

        for (int i = 0; i < slotCount; i++) {
            int poolIndex = (rotationOffset + i) % poolSize;
            String materialName = poolOrder.get(poolIndex);
            BestPoolEntry pe = bestConfig.getPool().get(materialName);
            if (pe == null) continue;

            double factor = randomFactor();
            double customPrice = pe.getDefaultPrice() * factor;
            int slot = slots.get(i);

            activeItems.put(slot, new BestItem(pe.getMaterialName(), pe.getMaterial(), pe.getDefaultPrice(), pe.getLimit(), customPrice, pe.getPointsX1(), slot));
        }

        rotationOffset = (rotationOffset + slotCount) % poolSize;

        if (firstRotation) {
            firstRotation = false;
        } else {
            broadcastRotation();
        }
    }

    private void broadcastRotation() {
        List<String> template = plugin.getMessagesConfig().getMessageUpdateBestItem();
        if (template == null || template.isEmpty()) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            List<String> lines = template.stream()
                    .map(line -> PlaceholderAPIHook.apply(line, player))
                    .collect(Collectors.toList());
            ActionType.dispatchAll(plugin, player, lines);
        }
    }

    private double randomFactor() {
        double min = bestConfig.getMinFactor();
        double max = bestConfig.getMaxFactor();
        if (max <= min) return min;
        return min + (max - min) * random.nextDouble();
    }

    public boolean isBestMenu(@NotNull String menuName) {
        return "bestmenu".equalsIgnoreCase(menuName);
    }

    public boolean isBestTitle(@NotNull String title) {
        String configured = HexUtils.colorize(bestConfig.getMenuTitle());
        return configured != null && !configured.isEmpty() && configured.equals(title);
    }

    public void populate(@NotNull Inventory inv, @NotNull Player player) {
        for (Map.Entry<Integer, BestItem> e : activeItems.entrySet()) {
            int slot = e.getKey();
            BestItem item = e.getValue();
            if (slot >= inv.getSize()) continue;
            inv.setItem(slot, buildItemStack(item, player));
        }
    }

    @NotNull
    private ItemStack buildItemStack(@NotNull BestItem item, @NotNull Player player) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        String displayName = bestConfig.getTemplateName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = "&f" + item.getMaterialName();
        }
        meta.setDisplayName(applyBest(displayName, player, item));

        List<String> lore = bestConfig.getTemplateLore().stream()
                .map(line -> applyBest(line, player, item))
                .collect(Collectors.toList());
        meta.setLore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    @NotNull
    private String applyBest(@Nullable String input, @NotNull Player player, @NotNull BestItem item) {
        if (input == null) return "";
        UUID uuid = player.getUniqueId();
        int soldNow = plugin.getDataBase().getSoldAmount(uuid, item.getMaterialName());
        String withBest = PlaceholderAPIHook.applyBest(input, item, soldNow);
        return PlaceholderAPIHook.apply(withBest, player);
    }

    public void handleClick(@NotNull Player player, int slot, @NotNull ClickType clickType) {
        BestItem item = activeItems.get(slot);
        if (item == null) return;

        int soldNow = plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName());
        int remaining = Math.max(0, item.getLimit() - soldNow);

        if (remaining <= 0) {
            sendMessages(plugin.getMessagesConfig().getMessageLimitCompleted(), player, item, 0);
            playNoItemSound(player);
            return;
        }

        int requested;
        if (clickType == ClickType.LEFT) {
            requested = 1;
        } else if (clickType == ClickType.RIGHT) {
            if (remaining < 64) {
                sendMessages(plugin.getMessagesConfig().getMessageLessThan64Items(), player, item, remaining);
                playNoItemSound(player);
                return;
            }
            int inInv = InventoryUtils.count(player, item.getMaterial());
            if (inInv < 64) {
                sendNoItem(player, item, 64);
                playNoItemSound(player);
                return;
            }
            requested = 64;
        } else {
            int inInv = InventoryUtils.count(player, item.getMaterial());
            requested = Math.min(remaining, inInv);
            if (requested <= 0) {
                sendNoItem(player, item, remaining);
                playNoItemSound(player);
                return;
            }
        }

        processSale(player, item, requested);
    }

    private void processSale(@NotNull Player player, @NotNull BestItem item, int requestedAmount) {
        int cappedRequest = Math.min(requestedAmount, Math.max(0, item.getLimit() - plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName())));
        if (cappedRequest <= 0) {
            sendMessages(plugin.getMessagesConfig().getMessageLimitCompleted(), player, item, 0);
            playNoItemSound(player);
            return;
        }

        int actual = InventoryUtils.remove(player, item.getMaterial(), cappedRequest);
        if (actual <= 0) {
            sendNoItem(player, item, cappedRequest);
            playNoItemSound(player);
            return;
        }

        PlayerBooster booster = plugin.getDataBase().getPlayerData(player.getUniqueId());
        double moneyMultiplier = plugin.getBoosterManager().getMoneyMultiplier(booster);
        double totalPrice = item.getCustomPrice() * actual * moneyMultiplier;

        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(player, totalPrice);

        long pointsEarned = (long) item.getPointsX1() * actual;
        plugin.getBoosterManager().addPointsAndCheckLevelUp(player, booster, pointsEarned);

        plugin.getDataBase().addSoldAmount(player.getUniqueId(), item.getMaterialName(), actual);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDataBase().save(booster));

        Entry pseudoEntry = new Entry(item.getMaterialName(), item.getMaterial(), item.getMaterialName(), item.getMaterialName(), Collections.emptyList(), item.getCustomPrice(), item.getCustomPrice64(), item.getSlot());
        List<String> lines = plugin.getMessagesConfig().getMessageSuccessfullyBuyer().stream()
                .map(line -> {
                    int soldNow = plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName());
                    String withBest = PlaceholderAPIHook.applyBest(line, item, soldNow);
                    return PlaceholderAPIHook.apply(withBest, player, pseudoEntry, actual, totalPrice);
                })
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, player, lines);

        playExchangeSound(player);
        refreshOpenMenu(player);
    }

    private void refreshOpenMenu(@NotNull Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            String title = player.getOpenInventory().getTitle();
            if (isBestTitle(title)) {
                populate(player.getOpenInventory().getTopInventory(), player);
            }
        });
    }

    private void sendNoItem(@NotNull Player player, @NotNull BestItem item, int amount) {
        Entry pseudoEntry = new Entry(item.getMaterialName(), item.getMaterial(), item.getMaterialName(), item.getMaterialName(), Collections.emptyList(), item.getCustomPrice(), item.getCustomPrice64(), item.getSlot());
        List<String> lines = plugin.getMessagesConfig().getMessageNoItem().stream()
                .map(line -> {
                    int soldNow = plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName());
                    String withBest = PlaceholderAPIHook.applyBest(line, item, soldNow);
                    return PlaceholderAPIHook.apply(withBest, player, pseudoEntry, amount);
                })
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, player, lines);
    }

    private void sendMessages(@NotNull List<String> template, @NotNull Player player, @NotNull BestItem item, int amount) {
        Entry pseudoEntry = new Entry(item.getMaterialName(), item.getMaterial(), item.getMaterialName(), item.getMaterialName(), Collections.emptyList(), item.getCustomPrice(), item.getCustomPrice64(), item.getSlot());
        List<String> lines = template.stream()
                .map(line -> {
                    int soldNow = plugin.getDataBase().getSoldAmount(player.getUniqueId(), item.getMaterialName());
                    String withBest = PlaceholderAPIHook.applyBest(line, item, soldNow);
                    return PlaceholderAPIHook.apply(withBest, player, pseudoEntry, amount);
                })
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, player, lines);
    }

    private void playNoItemSound(@NotNull Player player) {
        if (!plugin.getConfigManager().isSoundNoItemEnabled()) return;
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundNoItem());
            player.playSound(player.getLocation(), sound,
                    plugin.getConfigManager().getSoundNoItemVolume(),
                    plugin.getConfigManager().getSoundNoItemPitch());
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void playExchangeSound(@NotNull Player player) {
        if (!plugin.getConfigManager().isSoundExchangeEnabled()) return;
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundExchange());
            player.playSound(player.getLocation(), sound,
                    plugin.getConfigManager().getSoundExchangeVolume(),
                    plugin.getConfigManager().getSoundExchangePitch());
        } catch (IllegalArgumentException ignored) {
        }
    }
}
