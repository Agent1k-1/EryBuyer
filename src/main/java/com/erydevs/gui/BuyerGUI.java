package com.erydevs.gui;

import com.erydevs.gui.entry.Entry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.erydevs.EryBuyer;
import com.erydevs.config.Configs;
import com.erydevs.gui.button.ButtonConfig;
import com.erydevs.gui.menu.MenuRegistry;
import com.erydevs.gui.menu.MenuLoaderService;
import com.erydevs.gui.menu.ItemStackService;
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
    private final MenuRegistry menuRegistry;
    private final MenuLoaderService menuLoader;
    private final ItemStackService itemStackFactory;
    private final Map<Integer, Entry> combinedSlotMap = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByTitle = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByTitle = new HashMap<>();
    private final Map<String, String> menuNameByTitle = new HashMap<>();

    public BuyerGUI(@NotNull EryBuyer plugin, @NotNull Configs configManager, @NotNull MenuRegistry menuRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.menuRegistry = menuRegistry;
        this.menuLoader = new MenuLoaderService(plugin, combinedSlotMap, entriesByTitle, actionsByTitle, menuNameByTitle);
        this.itemStackFactory = new ItemStackService(plugin);
        menuLoader.loadAllMenus();
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player, @NotNull String menuName) {
        FileConfiguration cfg = menuRegistry.getMenuConfig(menuName);
        String title = HexUtils.colorize(cfg.getString("name", menuName));
        int size = cfg.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);

        itemStackFactory.addPanels(inv, cfg, size);

        Map<Integer, ButtonConfig> buttons = loadButtons(cfg);
        addItems(inv, cfg, title, size, player, buttons);

        return inv;
    }

    private void addItems(@NotNull Inventory inv, @NotNull FileConfiguration cfg, @NotNull String title, int size, @NotNull Player player, @NotNull Map<Integer, ButtonConfig> buttons) {
        Map<Integer, Entry> entries = MenuLoaderService.loadItemSettings(cfg, combinedSlotMap);

        for (Map.Entry<Integer, ButtonConfig> entry : buttons.entrySet()) {
            ButtonConfig btn = entry.getValue();
            if (btn.getSlot() >= size) continue;

            Entry it = btn.getMaterialStr() != null && !btn.getMaterialStr().isEmpty()
                    ? new Entry(btn.getId(), btn.getMaterial(), btn.getMaterialStr(), btn.getName(), btn.getLore(), 0.0, 0.0, btn.getSlot())
                    : new Entry(btn.getId(), btn.getMaterial(), btn.getName(), btn.getLore(), 0.0, 0.0, btn.getSlot());

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

            ButtonConfig btn = pm.isCustomHead()
                    ? new ButtonConfig(key, slot, pm.getMaterial(), pm.getHeadTextureBase64(), name, lore, actions)
                    : new ButtonConfig(key, slot, pm.getMaterial(), name, lore, actions);
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

    public void reloadMenus() {
        menuRegistry.reload();
        combinedSlotMap.clear();
        entriesByTitle.clear();
        actionsByTitle.clear();
        menuNameByTitle.clear();
        menuLoader.loadAllMenus();
        SkullUtils.clearCache();
    }
}