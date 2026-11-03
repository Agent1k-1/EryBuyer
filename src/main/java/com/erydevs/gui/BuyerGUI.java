package com.erydevs.gui;

import com.erydevs.EryBuyer;
import com.erydevs.gui.holder.MenuHolder;
import com.erydevs.config.Configs;
import com.erydevs.gui.button.ButtonConfig;
import com.erydevs.gui.button.ButtonLoader;
import com.erydevs.gui.entry.Entry;
import com.erydevs.gui.loader.MenuLoader;
import com.erydevs.gui.service.ItemStackService;
import com.erydevs.gui.service.MenuLoaderService;
import com.erydevs.utils.HexUtils;
import com.erydevs.utils.head.SkullUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuyerGUI {
    private final EryBuyer plugin;
    private final Configs configManager;
    private final MenuLoader menuLoader;
    private final MenuLoaderService menuLoaderService;
    private final ItemStackService itemStackFactory;
    private final Map<Integer, Entry> combinedSlotMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByMenu = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByMenu = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Entry>> templateByMenu = new ConcurrentHashMap<>();

    public BuyerGUI(@NotNull EryBuyer plugin, @NotNull Configs configManager, @NotNull MenuLoader menuLoader) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.menuLoader = menuLoader;
        this.menuLoaderService = new MenuLoaderService(plugin, combinedSlotMap, entriesByMenu, actionsByMenu);
        this.itemStackFactory = new ItemStackService(plugin);
        this.menuLoaderService.loadAllMenus();
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player, @NotNull String menuName) {
        if (!plugin.getAutoBuyerSettings().isLoaded(player.getUniqueId())) {
            plugin.getAutoBuyerSettings().runForPlayer(player, () -> refreshOpenInventory(player));
        }
        boolean isBest = plugin.getBestManager() != null && plugin.getBestManager().isBestMenu(menuName);
        if (isBest && !plugin.getBestManager().isEnabledInConfig()) {
            menuName = "menu";
            isBest = false;
        }

        FileConfiguration cfg = menuLoader.getMenuConfig(menuName);
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        MenuHolder holder = new MenuHolder(menuName);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.bind(inv);

        itemStackFactory.addPanels(inv, cfg, size);

        Map<Integer, Entry> entries = getTemplate(cfg, menuName, size, isBest);
        entriesByMenu.put(menuName, entries);

        for (Entry it : entries.values()) {
            if (it.slot < size) inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
        }

        if (isBest) {
            plugin.getBestManager().populate(inv, player);
        }

        return inv;
    }

    @NotNull
    private Map<Integer, Entry> getTemplate(@NotNull FileConfiguration cfg, @NotNull String menuName, int size, boolean isBest) {
        Map<Integer, Entry> cached = templateByMenu.get(menuName);
        if (cached != null) return cached;

        Map<Integer, Entry> entries = MenuLoaderService.loadItemSettings(cfg, null);

        Set<Integer> bestSlots = isBest
                ? new HashSet<>(plugin.getBestManager().getBestConfig().getSlots())
                : Collections.emptySet();

        if (isBest) {
            entries.keySet().removeAll(bestSlots);
        }

        for (ButtonConfig btn : ButtonLoader.load(cfg).values()) {
            if (btn.getSlot() >= size) continue;
            if (bestSlots.contains(btn.getSlot())) continue;

            entries.put(btn.getSlot(), new Entry(btn.getId(), btn.getMaterial(), btn.getMaterialStr(),
                    btn.getName(), btn.getLore(),
                    btn.getPriceX1(), btn.getPriceX64(), btn.getPointsX1(), btn.getTokensX1(), btn.getSlot(),
                    null, btn.getCustomModelData()));
        }

        templateByMenu.put(menuName, entries);
        return entries;
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player) {
        return createInventory(player, "menu");
    }

    @Nullable
    public Entry getEntry(@NotNull String menuId, int slot) {
        Map<Integer, Entry> m = entriesByMenu.get(menuId);
        return m != null ? m.get(slot) : null;
    }

    @NotNull
    public Map<Integer, Entry> getSlotMap() {
        return combinedSlotMap;
    }

    @NotNull
    public Collection<Entry> getAllEntries() {
        List<Entry> out = new ArrayList<>();
        for (Map<Integer, Entry> m : entriesByMenu.values()) {
            out.addAll(m.values());
        }
        return out;
    }

    @NotNull
    public Collection<Entry> getBuyableEntries() {
        return combinedSlotMap.values();
    }

    @Nullable
    public String getMenuId(@NotNull Inventory inventory) {
        if (!(inventory.getHolder() instanceof MenuHolder)) return null;
        return ((MenuHolder) inventory.getHolder()).getMenuId();
    }

    @Nullable
    public List<String> getActions(@NotNull String menuId, int slot) {
        Map<Integer, List<String>> actions = actionsByMenu.get(menuId);
        return actions != null ? actions.get(slot) : null;
    }

    public void refreshOpenInventory(@NotNull Player player) {
        String menuName = getMenuId(player.getOpenInventory().getTopInventory());
        if (menuName == null) return;

        Map<Integer, Entry> entries = entriesByMenu.get(menuName);
        if (entries == null) return;

        Inventory inv = player.getOpenInventory().getTopInventory();
        int size = inv.getSize();

        for (Entry it : entries.values()) {
            if (it.slot < size) inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
        }

        if (plugin.getBestManager() != null && plugin.getBestManager().isBestMenu(menuName)) {
            plugin.getBestManager().populate(inv, player);
        }
    }

    public void reloadMenus() {
        menuLoader.reload();
        combinedSlotMap.clear();
        entriesByMenu.clear();
        actionsByMenu.clear();
        templateByMenu.clear();
        menuLoaderService.loadAllMenus();
        SkullUtils.clearCache();
    }
}
