package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.config.Configs;
import com.erydevs.db.Database;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AutoBuyerSettingsTest {
    private EryBuyer plugin;
    private Database database;
    private YamlConfiguration config;
    private AutoBuyerSettingsManager settings;

    @Before
    public void setup() {
        plugin = mock(EryBuyer.class);
        database = mock(Database.class);
        Configs configs = mock(Configs.class);
        config = new YamlConfiguration();
        when(plugin.getDataBase()).thenReturn(database);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(plugin.getConfigManager()).thenReturn(configs);
        when(configs.getConfig()).thenReturn(config);
        when(database.loadAutoBuyerSettings(any())).thenReturn(Collections.emptyMap());
        settings = new AutoBuyerSettingsManager(plugin);
    }

    @After
    public void cleanup() {
        settings.close();
    }

    @Test
    public void unloadedSettingsNeverAllowSale() {
        UUID uuid = UUID.randomUUID();
        assertFalse(settings.isLoaded(uuid));
        assertFalse(settings.isItemEnabled(uuid, Material.DIAMOND));
    }

    @Test
    public void playersHaveIndependentFilters() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        settings.load(first).get(5, TimeUnit.SECONDS);
        settings.load(second).get(5, TimeUnit.SECONDS);
        settings.setItemEnabled(first, Material.DIAMOND, false);
        assertFalse(settings.isItemEnabled(first, Material.DIAMOND));
        assertTrue(settings.isItemEnabled(first, Material.EMERALD));
        assertTrue(settings.isItemEnabled(second, Material.DIAMOND));
    }

    @Test
    public void savedChoicesOverrideEnabledDefault() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(database.loadAutoBuyerSettings(uuid)).thenReturn(Collections.singletonMap("DIAMOND", false));
        settings.load(uuid).get(5, TimeUnit.SECONDS);
        assertFalse(settings.isItemEnabled(uuid, Material.DIAMOND));
        assertTrue(settings.isItemEnabled(uuid, Material.EMERALD));
    }

    @Test
    public void failedLoadCanBeRetriedAndDoesNotEnableSales() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(database.loadAutoBuyerSettings(uuid)).thenThrow(new IllegalStateException("offline"))
                .thenReturn(Collections.singletonMap("DIAMOND", false));
        try {
            settings.load(uuid).get(5, TimeUnit.SECONDS);
            fail();
        } catch (ExecutionException expected) {
            assertFalse(settings.isLoaded(uuid));
            assertFalse(settings.isItemEnabled(uuid, Material.EMERALD));
        }
        settings.load(uuid).get(5, TimeUnit.SECONDS);
        assertFalse(settings.isItemEnabled(uuid, Material.DIAMOND));
        assertTrue(settings.isItemEnabled(uuid, Material.EMERALD));
    }

    @Test
    public void pendingLoadNeverAllowsSale() throws Exception {
        UUID uuid = UUID.randomUUID();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(database.loadAutoBuyerSettings(uuid)).thenAnswer(invocation -> {
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
            return Collections.singletonMap("DIAMOND", false);
        });
        CompletableFuture<Void> future = settings.load(uuid);
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertFalse(settings.isItemEnabled(uuid, Material.DIAMOND));
            assertFalse(settings.isItemEnabled(uuid, Material.EMERALD));
        } finally {
            release.countDown();
        }
        future.get(5, TimeUnit.SECONDS);
        assertFalse(settings.isItemEnabled(uuid, Material.DIAMOND));
    }

    @Test
    public void rapidChangesAreSavedInOrderBeforeShutdown() throws Exception {
        UUID uuid = UUID.randomUUID();
        settings.load(uuid).get(5, TimeUnit.SECONDS);
        settings.setItemEnabled(uuid, Material.DIAMOND, false);
        settings.setItemEnabled(uuid, Material.DIAMOND, true);
        settings.setItemEnabled(uuid, Material.DIAMOND, false);
        settings.close();
        org.mockito.InOrder order = inOrder(database);
        order.verify(database).saveAutoBuyerSetting(uuid, "DIAMOND", false);
        order.verify(database).saveAutoBuyerSetting(uuid, "DIAMOND", true);
        order.verify(database).saveAutoBuyerSetting(uuid, "DIAMOND", false);
        settings = new AutoBuyerSettingsManager(plugin);
    }
    @Test
    public void bulkToggleOnlyChangesSelectedPlayer() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        settings.load(first).get(5, TimeUnit.SECONDS);
        settings.load(second).get(5, TimeUnit.SECONDS);
        com.erydevs.gui.BuyerGUI gui = mock(com.erydevs.gui.BuyerGUI.class);
        when(plugin.getBuyerGUI()).thenReturn(gui);
        when(gui.getBuyableEntries()).thenReturn(Arrays.asList(
                new com.erydevs.gui.entry.Entry("diamond", Material.DIAMOND, null, "Diamond", Collections.emptyList(), 1, 64, 1, 1, 20),
                new com.erydevs.gui.entry.Entry("emerald", Material.EMERALD, null, "Emerald", Collections.emptyList(), 1, 64, 1, 1, 21)));
        settings.setAllItemsEnabled(first, false);
        assertFalse(settings.isItemEnabled(first, Material.DIAMOND));
        assertFalse(settings.isItemEnabled(first, Material.EMERALD));
        assertTrue(settings.isItemEnabled(second, Material.DIAMOND));
        settings.setAllItemsEnabled(first, true);
        assertTrue(settings.isItemEnabled(first, Material.DIAMOND));
        assertTrue(settings.isItemEnabled(first, Material.EMERALD));
    }



}
