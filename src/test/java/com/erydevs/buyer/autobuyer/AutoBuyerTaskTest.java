package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.autobuyer.task.AutoBuyerTask;
import com.erydevs.config.Configs;
import com.erydevs.folia.Scheduler;
import com.erydevs.gui.BuyerGUI;
import com.erydevs.gui.entry.Entry;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.mockito.Mockito.*;

public class AutoBuyerTaskTest {
    private EryBuyer plugin;
    private AutoBuyerManager manager;
    private AutoBuyerSettingsManager settings;
    private Player player;
    private AutoBuyerTask task;
    private final AtomicReference<Runnable> tick = new AtomicReference<>();

    @Before
    public void setup() throws Exception {
        plugin = mock(EryBuyer.class);
        manager = mock(AutoBuyerManager.class);
        com.erydevs.db.Database database = mock(com.erydevs.db.Database.class);
        when(plugin.getDataBase()).thenReturn(database);
        when(database.loadAutoBuyerSettings(any())).thenReturn(Collections.singletonMap("DIAMOND", false));
        settings = new AutoBuyerSettingsManager(plugin);
        player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.isOnline()).thenReturn(true);
        Server server = mock(Server.class);
        java.lang.reflect.Field serverField = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(plugin, server);
        when(server.getPlayer(uuid)).thenReturn(player);
        Configs configs = mock(Configs.class);
        when(configs.getConfig()).thenReturn(new org.bukkit.configuration.file.YamlConfiguration());
        when(plugin.getConfigManager()).thenReturn(configs);
        when(configs.getAutobuyerTime()).thenReturn(40L);
        Scheduler scheduler = mock(Scheduler.class);
        when(plugin.getScheduler()).thenReturn(scheduler);
        when(scheduler.runGlobalTimer(any(), anyLong(), anyLong())).thenAnswer(invocation -> {
            tick.set(invocation.getArgument(0));
            return (com.erydevs.folia.SchedulerTask) () -> {};
        });
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        }).when(scheduler).runForPlayer(eq(player), any());
        BuyerGUI gui = mock(BuyerGUI.class);
        when(plugin.getBuyerGUI()).thenReturn(gui);
        Entry diamond = new Entry("diamond", Material.DIAMOND, null, "Diamond", Collections.emptyList(), 250, 16000, 15, 3, 20);
        when(gui.getBuyableEntries()).thenReturn(Collections.singleton(diamond));
        when(plugin.getAutoBuyerSettings()).thenReturn(settings);
        when(manager.isAutobuyerEnabled(player)).thenReturn(true);

        task = new AutoBuyerTask(plugin, manager);
        task.start();
    }

    @After
    public void cleanup() {
        settings.close();
    }

    @Test
    public void disabledItemIsNeverRemoved() throws Exception {
        settings.load(player.getUniqueId()).get(5, java.util.concurrent.TimeUnit.SECONDS);
        task.markDirty(player.getUniqueId());
        tick.get().run();
        verify(player, never()).getInventory();
    }

    @Test
    public void switchedOffAutobuyerDoesNotProcessQueuedPlayer() {
        when(manager.isAutobuyerEnabled(player)).thenReturn(false);
        task.markDirty(player.getUniqueId());
        tick.get().run();
        verify(player, never()).getInventory();
    }

    @Test
    public void taskWaitsForSettingsBeforeReadingInventory() {

        task.markDirty(player.getUniqueId());
        tick.get().run();
        verify(player, never()).getInventory();
    }
}
