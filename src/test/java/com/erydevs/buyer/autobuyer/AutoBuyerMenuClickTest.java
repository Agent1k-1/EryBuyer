package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.config.Configs;
import com.erydevs.db.Database;
import com.erydevs.folia.Scheduler;
import com.erydevs.gui.BuyerGUI;
import com.erydevs.gui.entry.Entry;
import com.erydevs.listeners.InventoryListener;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AutoBuyerMenuClickTest {
    private AutoBuyerSettingsManager settings;
    private Player player;
    private InventoryClickEvent event;
    private InventoryListener listener;
    private BuyerGUI gui;
    private EryBuyer plugin;

    @Before
    public void setup() throws Exception {
        plugin = mock(EryBuyer.class);
        java.lang.reflect.Field enabled = JavaPlugin.class.getDeclaredField("isEnabled");
        enabled.setAccessible(true);
        enabled.set(plugin, true);
        Database database = mock(Database.class);
        when(plugin.getDataBase()).thenReturn(database);
        when(database.loadAutoBuyerSettings(any())).thenReturn(Collections.emptyMap());
        Configs configs = mock(Configs.class);
        when(plugin.getConfigManager()).thenReturn(configs);
        when(configs.getConfig()).thenReturn(new YamlConfiguration());
        Scheduler scheduler = mock(Scheduler.class);
        when(plugin.getScheduler()).thenReturn(scheduler);
        doAnswer(call -> {
            ((Runnable) call.getArgument(1)).run();
            return null;
        }).when(scheduler).runForPlayer(any(), any());
        AutoBuyerManager manager = mock(AutoBuyerManager.class);
        when(plugin.getAutoBuyerManager()).thenReturn(manager);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.isOnline()).thenReturn(true);
        when(player.hasPermission("erybuyer.autobuyer")).thenReturn(true);
        when(manager.isAutobuyerEnabled(player)).thenReturn(true);
        settings = new AutoBuyerSettingsManager(plugin);
        when(plugin.getAutoBuyerSettings()).thenReturn(settings);
        settings.load(player.getUniqueId()).get(5, TimeUnit.SECONDS);
        gui = mock(BuyerGUI.class);
        when(plugin.getBuyerGUI()).thenReturn(gui);
        when(gui.getEntry("farma", 20)).thenReturn(new Entry("sugar", Material.SUGAR_CANE, null,
                "Sugar", Collections.emptyList(), 90, 5760, 1, 1, 20));
        Inventory inventory = mock(Inventory.class);
        when(inventory.getSize()).thenReturn(45);
        when(gui.getMenuId(inventory)).thenReturn("farma");
        InventoryView view = mock(InventoryView.class);
        when(view.getTitle()).thenReturn("Farm");
        when(view.getTopInventory()).thenReturn(inventory);
        event = mock(InventoryClickEvent.class);
        when(event.getView()).thenReturn(view);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlot()).thenReturn(20);
        listener = new InventoryListener(plugin);
    }

    @After
    public void close() {
        settings.close();
    }

    @Test
    public void sameTitleMenusUseTheirOwnEntries() {
        Inventory otherInventory = mock(Inventory.class);
        when(otherInventory.getSize()).thenReturn(45);
        when(gui.getMenuId(otherInventory)).thenReturn("mine");
        when(gui.getEntry("mine", 20)).thenReturn(new Entry("diamond", Material.DIAMOND, null,
                "Diamond", Collections.emptyList(), 325, 20800, 15, 3, 20));
        when(event.getClick()).thenReturn(ClickType.DROP);
        listener.onInventoryClick(event);
        when(event.getView().getTopInventory()).thenReturn(otherInventory);
        listener.onInventoryClick(event);
        assertFalse(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        assertFalse(settings.isItemEnabled(player.getUniqueId(), Material.DIAMOND));
        verify(gui).getEntry("farma", 20);
        verify(gui).getEntry("mine", 20);
    }

    @Test
    public void unrelatedInventoryWithSameTitleIsIgnored() {
        when(gui.getMenuId(event.getView().getTopInventory())).thenReturn(null);
        when(event.getClick()).thenReturn(ClickType.DROP);
        listener.onInventoryClick(event);
        verify(event, never()).setCancelled(true);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
    }

    @Test
    public void qTogglesItemWhileAutobuyerIsActiveWithoutSelling() {
        when(event.getClick()).thenReturn(ClickType.DROP);
        listener.onInventoryClick(event);
        assertFalse(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        listener.onInventoryClick(event);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        verify(event, times(2)).setCancelled(true);
        verify(player, never()).getInventory();
    }

    @Test
    public void controlQDoesNotToggleOrSell() {
        when(event.getClick()).thenReturn(ClickType.CONTROL_DROP);
        listener.onInventoryClick(event);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        verify(player, never()).getInventory();
    }

    @Test
    public void qWithoutPermissionKeepsFilter() {
        when(event.getClick()).thenReturn(ClickType.DROP);
        when(player.hasPermission("erybuyer.autobuyer")).thenReturn(false);
        listener.onInventoryClick(event);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        verify(player, never()).getInventory();
    }

    @Test
    public void qOnBottomInventoryDoesNotToggle() {
        when(event.getClick()).thenReturn(ClickType.DROP);
        when(event.getRawSlot()).thenReturn(65);
        listener.onInventoryClick(event);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        verify(gui, never()).getEntry("farma", 65);
    }
    @Test
    public void qInBestMenuNeverTogglesOrSells() {
        com.erydevs.buyer.best.BestManager best = mock(com.erydevs.buyer.best.BestManager.class);
        when(plugin.getBestManager()).thenReturn(best);
        when(best.isBestMenu("farma")).thenReturn(true);
        when(event.getClick()).thenReturn(ClickType.DROP);
        listener.onInventoryClick(event);
        assertTrue(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        settings.setItemEnabled(player.getUniqueId(), Material.SUGAR_CANE, false);
        listener.onInventoryClick(event);
        assertFalse(settings.isItemEnabled(player.getUniqueId(), Material.SUGAR_CANE));
        verify(player, never()).getInventory();
        verify(best, never()).handleClick(any(), anyInt(), any());
    }

    @Test
    public void rightClickWithLessThanStackShowsMissingItemMessage() {
        when(plugin.getAutoBuyerManager().isAutobuyerEnabled(player)).thenReturn(false);
        when(event.getClick()).thenReturn(ClickType.RIGHT);
        com.erydevs.config.MessagesConfig messages = mock(com.erydevs.config.MessagesConfig.class);
        when(plugin.getMessagesConfig()).thenReturn(messages);
        org.bukkit.inventory.PlayerInventory inventory = mock(org.bukkit.inventory.PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContents()).thenReturn(new org.bukkit.inventory.ItemStack[]{new org.bukkit.inventory.ItemStack(Material.SUGAR_CANE, 3)});
        listener.onInventoryClick(event);
        verify(messages).getMessageNoItem();
        verify(inventory, never()).setItem(anyInt(), any(org.bukkit.inventory.ItemStack.class));
    }

}
