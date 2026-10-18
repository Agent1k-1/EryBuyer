package com.erydevs.papi;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.autobuyer.AutoBuyerManager;
import com.erydevs.buyer.autobuyer.AutoBuyerSettingsManager;
import com.erydevs.buyer.best.BestItem;
import com.erydevs.config.Configs;
import com.erydevs.config.MessagesConfig;
import com.erydevs.db.Database;
import com.erydevs.gui.entry.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AutobuyerPlaceholderTest {
    private AutoBuyerSettingsManager settings;
    private Player player;
    private Field instanceField;
    private Field serverField;
    private Object previousInstance;
    private Object previousServer;
    private Entry entry;
    private EryBuyer plugin;
    private Database database;
    private MessagesConfig messages;

    @Before
    public void setup() throws Exception {
        plugin = mock(EryBuyer.class);
        database = mock(Database.class);
        when(plugin.getDataBase()).thenReturn(database);
        when(database.loadAutoBuyerSettings(any())).thenReturn(Collections.emptyMap());
        Configs configs = mock(Configs.class);
        when(plugin.getConfigManager()).thenReturn(configs);
        when(configs.getConfig()).thenReturn(new YamlConfiguration());
        when(plugin.getAutoBuyerManager()).thenReturn(mock(AutoBuyerManager.class));
        messages = mock(MessagesConfig.class);
        when(plugin.getMessagesConfig()).thenReturn(messages);
        when(messages.getPlaceholderDisableAutobuyer()).thenReturn("off");
        when(messages.getPlaceholderEnableAutobuyer()).thenReturn("on");
        when(messages.getPlaceholderEnableItemAutobuyer()).thenReturn("&a✔ [Включено]");
        when(messages.getPlaceholderDisableItemAutobuyer()).thenReturn("&c✘ [Выключено]");
        settings = new AutoBuyerSettingsManager(plugin);
        when(plugin.getAutoBuyerSettings()).thenReturn(settings);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        instanceField = EryBuyer.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        previousInstance = instanceField.get(null);
        instanceField.set(null, plugin);
        serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        previousServer = serverField.get(null);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        serverField.set(null, server);
        entry = new Entry("diamond", Material.DIAMOND, null, "Diamond", Collections.emptyList(), 1, 64, 1, 1, 20);
    }

    @After
    public void cleanup() throws Exception {
        settings.close();
        instanceField.set(null, previousInstance);
        serverField.set(null, previousServer);
    }

    @Test
    public void ordinaryItemsResolveEnabledAndDisabledState() throws Exception {
        settings.load(player.getUniqueId()).get(5, TimeUnit.SECONDS);
        assertTrue(Placeholders.apply("%autobuyer_item_status%", player, entry, 1).contains("Включено"));
        settings.setItemEnabled(player.getUniqueId(), Material.DIAMOND, false);
        assertTrue(Placeholders.apply("%autobuyer_item_status%", player, entry, 1).contains("Выключено"));
    }

    @Test
    public void bestItemsResolveStateUsingTheirMaterial() throws Exception {
        settings.load(player.getUniqueId()).get(5, TimeUnit.SECONDS);
        settings.setItemEnabled(player.getUniqueId(), Material.DIAMOND, false);
        BestItem item = new BestItem("DIAMOND", Material.DIAMOND, 1, 64, 2, 1, 1, 20);
        assertTrue(Placeholders.applyBest("%autobuyer_item_status%", player, item, 0).contains("Выключено"));
    }

    @Test
    public void unloadedItemsResolveLoadingState() {
        assertTrue(Placeholders.apply("%autobuyer_item_status%", player, entry, 1).contains("Загрузка"));
    }

    @Test
    public void textWithoutPlaceholderRemainsUnchanged() {
        assertEquals("Price", Placeholders.apply("Price", player, entry, 1));
    }
    @Test
    public void boosterStatusWorksWithoutPlaceholderAPI() {
        com.erydevs.buyer.boosters.PlayerBooster booster = new com.erydevs.buyer.boosters.PlayerBooster(player.getUniqueId(), 0, 123);
        when(database.getPlayerData(player.getUniqueId())).thenReturn(booster);
        assertTrue(Placeholders.apply("%buyer_booster_status%", player).contains("Нет бустера"));
        booster.setCurrentLevel(3);
        assertTrue(Placeholders.apply("%buyer_booster_status%", player).contains("3 ур."));
        assertEquals("123", Placeholders.apply("%buyer_points%", player));
    }

    @Test
    public void boosterStatusIsAvailableThroughExpansion() {
        UUID uuid = player.getUniqueId();
        com.erydevs.buyer.boosters.PlayerBooster booster = new com.erydevs.buyer.boosters.PlayerBooster(uuid, 2, 0);
        when(database.getPlayerData(uuid)).thenReturn(booster);
        assertTrue(new PlaceholderAPIHook(plugin).onPlaceholderRequest(player, "booster_status").contains("2 ур."));
    }

    @Test
    public void bestFactorUsesActualPriceAndHandlesZeroBasePrice() {
        BestItem item = new BestItem("DIAMOND", Material.DIAMOND, 100, 180, 129, 1, 1, 20);
        assertEquals("1.29", Placeholders.applyBest("%custom-item-factor%", item, 0).replace(',', '.'));
        BestItem free = new BestItem("DIAMOND", Material.DIAMOND, 0, 180, 129, 1, 1, 20);
        assertEquals("1.00", Placeholders.applyBest("%custom-item-factor%", free, 0).replace(',', '.'));
    }

    @Test
    public void timerResolvesThroughInternalAndExpansionPaths() {
        com.erydevs.shop.ShopManager shop = mock(com.erydevs.shop.ShopManager.class);
        when(plugin.getShopManager()).thenReturn(shop);
        UUID uuid = player.getUniqueId();
        when(shop.getRemainingBoosterSeconds(uuid)).thenReturn(123L);
        assertEquals("02:03", Placeholders.apply("%buyer_booster_timer%", player));
        assertEquals("02:03", new PlaceholderAPIHook(plugin).onPlaceholderRequest(player, "booster_timer"));
    }

    @Test
    public void missingItemMessageUsesConfiguredGrammaticalName() {
        Entry carrot = new Entry("carrot", Material.CARROT, null, "&b✦ Морковь", Collections.emptyList(), 40, 2560, 1, 1, 21, "моркови");
        String text = Placeholders.apply("У вас недостаточно %item_name% для продажи!", player, carrot, 64);
        assertEquals("У вас недостаточно моркови для продажи!", text);
    }

    @Test
    public void entryWithoutMessageNameKeepsExistingNameBehavior() {
        assertEquals("Diamond", Placeholders.apply("%item_name%", player, entry, 1));
    }

    @Test
    public void itemStatusUsesMessagesConfigTexts() throws Exception {
        settings.load(player.getUniqueId()).get(5, TimeUnit.SECONDS);
        when(messages.getPlaceholderEnableItemAutobuyer()).thenReturn("&6Разрешено");
        when(messages.getPlaceholderDisableItemAutobuyer()).thenReturn("&4Запрещено");
        assertTrue(Placeholders.apply("%autobuyer_item_status%", player, entry, 1).contains("Разрешено"));
        settings.setItemEnabled(player.getUniqueId(), Material.DIAMOND, false);
        assertTrue(Placeholders.apply("%autobuyer_item_status%", player, entry, 1).contains("Запрещено"));
    }

}
