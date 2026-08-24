package com.erydevs.gui;

import com.erydevs.gui.entry.Entry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configs;
import com.erydevs.gui.button.ButtonConfig;
import com.erydevs.gui.button.ButtonLoader;
import com.erydevs.gui.loader.MenuLoader;
import com.erydevs.gui.service.MenuLoaderService;
import com.erydevs.gui.service.ItemStackService;
import com.erydevs.utils.head.SkullUtils;
import com.erydevs.utils.HexUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class BuyerGUI {
    private final EryBuyer plugin;
    private final Configs configManager;
    private final MenuLoader menuLoader;
    private final MenuLoaderService menuLoaderService;
    private final ItemStackService itemStackFactory;
    private final Map<Integer, Entry> combinedSlotMap = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByTitle = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByTitle = new HashMap<>();
    private final Map<String, String> menuNameByTitle = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> templateByMenu = new HashMap<>();

    public BuyerGUI(@NotNull EryBuyer plugin, @NotNull Configs configManager, @NotNull MenuLoader menuLoader) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.menuLoader = menuLoader;
        this.menuLoaderService = new MenuLoaderService(plugin, combinedSlotMap, entriesByTitle, actionsByTitle, menuNameByTitle);
        this.itemStackFactory = new ItemStackService(plugin);
        this.menuLoaderService.loadAllMenus();
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player, @NotNull String menuName) {
        boolean isBest = plugin.getBestManager() != null && plugin.getBestManager().isBestMenu(menuName);
        if (isBest && !plugin.getBestManager().isEnabledInConfig()) {
            menuName = "menu";
            isBest = false;
        }

        FileConfiguration cfg = menuLoader.getMenuConfig(menuName);
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);

        itemStackFactory.addPanels(inv, cfg, size);

        Map<Integer, Entry> entries = getTemplate(cfg, menuName, size, isBest);
        entriesByTitle.put(title, entries);

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
                    btn.getPriceX1(), btn.getPriceX64(), btn.getPointsX1(), btn.getSlot()));
        }

        templateByMenu.put(menuName, entries);
        return entries;
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player) {
        return createInventory(player, "menu");
    }

    @Nullable
    public Entry getEntry(@NotNull String title, int slot) {
        Map<Integer, Entry> m = entriesByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    @NotNull
    public Map<Integer, Entry> getSlotMap() {
        return combinedSlotMap;
    }

    @NotNull
    public Collection<Entry> getAllEntries() {
        List<Entry> out = new ArrayList<>();
        for (Map<Integer, Entry> m : entriesByTitle.values()) {
            out.addAll(m.values());
        }
        return out;
    }

    @NotNull
    public Collection<Entry> getBuyableEntries() {
        return combinedSlotMap.values();
    }

    public boolean isManagedTitle(@NotNull String title) {
        return entriesByTitle.containsKey(title);
    }

    @Nullable
    public List<String> getActions(@NotNull String title, int slot) {
        Map<Integer, List<String>> m = actionsByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    @NotNull
    public String getMenuNameByTitle(@NotNull String title) {
        return menuNameByTitle.getOrDefault(title, "menu");
    }

    public void refreshOpenInventory(@NotNull Player player) {
        String title = player.getOpenInventory().getTitle();

        Map<Integer, Entry> entries = entriesByTitle.get(title);
        if (entries == null) return;

        Inventory inv = player.getOpenInventory().getTopInventory();
        int size = inv.getSize();

        for (Entry it : entries.values()) {
            if (it.slot < size) inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
        }

        String menuName = getMenuNameByTitle(title);
        if (plugin.getBestManager() != null && plugin.getBestManager().isBestMenu(menuName)) {
            plugin.getBestManager().populate(inv, player);
        }
    }

    public void reloadMenus() {
        menuLoader.reload();
        combinedSlotMap.clear();
        entriesByTitle.clear();
        actionsByTitle.clear();
        menuNameByTitle.clear();
        templateByMenu.clear();
        menuLoaderService.loadAllMenus();
        SkullUtils.clearCache();
    }
}
