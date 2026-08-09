package com.erydevs.gui;

import com.erydevs.gui.entry.Entry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configs;
import com.erydevs.gui.button.ButtonConfig;
import com.erydevs.gui.loader.MenuLoader;
import com.erydevs.gui.service.MenuLoaderService;
import com.erydevs.gui.service.ItemStackService;
import com.erydevs.utils.head.MaterialHeadParser;
import com.erydevs.utils.head.ParsedMaterial;
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

        Map<Integer, ButtonConfig> buttons = loadButtons(cfg);
        addItems(inv, cfg, title, size, player, buttons, isBest);

        if (isBest) {
            plugin.getBestManager().populate(inv, player);
        }

        return inv;
    }

    private void addItems(@NotNull Inventory inv, @NotNull FileConfiguration cfg, @NotNull String title, int size, @NotNull Player player, @NotNull Map<Integer, ButtonConfig> buttons, boolean isBest) {
        Map<Integer, Entry> entries = MenuLoaderService.loadItemSettings(cfg, combinedSlotMap);

        Set<Integer> bestSlots = isBest
                ? new HashSet<>(plugin.getBestManager().getBestConfig().getSlots())
                : Collections.emptySet();

        if (isBest) {
            entries.keySet().removeAll(bestSlots);
        }

        for (Map.Entry<Integer, ButtonConfig> entry : buttons.entrySet()) {
            ButtonConfig btn = entry.getValue();
            if (btn.getSlot() >= size) continue;
            if (bestSlots.contains(btn.getSlot())) continue;

            Entry it = new Entry(btn.getId(), btn.getMaterial(), btn.getMaterialStr(),
                    btn.getName(), btn.getLore(),
                    btn.getPriceX1(), btn.getPriceX64(), btn.getPointsX1(), btn.getSlot());

            entries.put(it.slot, it);
        }

        for (Map.Entry<Integer, Entry> e : entries.entrySet()) {
            Entry it = e.getValue();
            if (it.slot < size) inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
        }

        entriesByTitle.put(title, entries);
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

    @NotNull
    public Map<Integer, ButtonConfig> loadButtons(@NotNull FileConfiguration cfg) {
        Map<Integer, ButtonConfig> buttons = new HashMap<>();
        loadButtonsSection(cfg, "menu-settings", buttons);
        loadButtonsSection(cfg, "knops-settings", buttons);
        return buttons;
    }

    private void loadButtonsSection(@NotNull FileConfiguration cfg, @NotNull String section, @NotNull Map<Integer, ButtonConfig> buttons) {
        if (!cfg.isConfigurationSection(section)) return;

        org.bukkit.configuration.ConfigurationSection sect = cfg.getConfigurationSection(section);
        if (sect == null) return;

        for (String key : sect.getKeys(false)) {
            String path = section + "." + key;
            if (!cfg.contains(path + ".slot")) continue;

            int slot = cfg.getInt(path + ".slot");
            if (slot < 0) continue;

            ParsedMaterial pm = MaterialHeadParser.parse(cfg.getString(path + ".material"));
            if (pm == null) continue;

            String name = cfg.getString(path + ".name");
            List<String> lore = cfg.getStringList(path + ".lore");
            List<String> actions = cfg.getStringList(path + ".action");

            if ((actions == null || actions.isEmpty()) && cfg.contains(path + ".action")) {
                String single = cfg.getString(path + ".action");
                if (single != null) {
                    actions = Collections.singletonList(single);
                }
            }

            double priceX1 = cfg.getDouble(path + ".prince-x1");
            double priceX64 = cfg.getDouble(path + ".prince-x64");
            int pointsX1 = cfg.getInt(path + ".points-from-the-buyer-x1");

            ButtonConfig btn = new ButtonConfig(key, slot, pm.getMaterial(), pm.getHeadTextureBase64(),
                    name, lore, actions, priceX1, priceX64, pointsX1);
            buttons.put(slot, btn);
        }
    }

    @NotNull
    public Optional<ButtonConfig> findButtonByAction(@NotNull FileConfiguration cfg, @NotNull String actionFragment) {
        return loadButtons(cfg).values().stream()
                .filter(btn -> btn.hasAction(actionFragment))
                .findFirst();
    }

    public int findSlotByAction(@NotNull FileConfiguration cfg, @NotNull String actionFragment) {
        return findButtonByAction(cfg, actionFragment)
                .map(ButtonConfig::getSlot)
                .orElse(-1);
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
        if (!isManagedTitle(title)) return;

        String menuName = getMenuNameByTitle(title);
        FileConfiguration cfg = menuLoader.getMenuConfig(menuName);
        if (cfg == null) return;

        Inventory inv = player.getOpenInventory().getTopInventory();
        int size = cfg.getInt("size");

        boolean isBest = plugin.getBestManager() != null && plugin.getBestManager().isBestMenu(menuName);
        Set<Integer> bestSlots = isBest
                ? new HashSet<>(plugin.getBestManager().getBestConfig().getSlots())
                : Collections.emptySet();

        Map<Integer, Entry> entries = entriesByTitle.get(title);
        if (entries != null) {
            for (Entry it : entries.values()) {
                if (it.slot >= size) continue;
                if (bestSlots.contains(it.slot)) continue;
                inv.setItem(it.slot, itemStackFactory.createItemStack(it, player));
            }
        }

        for (Map.Entry<Integer, ButtonConfig> e : loadButtons(cfg).entrySet()) {
            ButtonConfig btn = e.getValue();
            if (btn.getSlot() >= size) continue;
            if (bestSlots.contains(btn.getSlot())) continue;

            Entry it = new Entry(btn.getId(), btn.getMaterial(), btn.getMaterialStr(),
                    btn.getName(), btn.getLore(),
                    btn.getPriceX1(), btn.getPriceX64(), btn.getPointsX1(), btn.getSlot());

            inv.setItem(btn.getSlot(), itemStackFactory.createItemStack(it, player));
        }

        if (isBest) {
            plugin.getBestManager().populate(inv, player);
        }
    }

    public void reloadMenus() {
        menuLoader.reload();
        combinedSlotMap.clear();
        entriesByTitle.clear();
        actionsByTitle.clear();
        menuNameByTitle.clear();
        menuLoaderService.loadAllMenus();
        SkullUtils.clearCache();
    }
}
