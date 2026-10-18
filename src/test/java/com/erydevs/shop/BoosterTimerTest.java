package com.erydevs.shop;

import com.erydevs.EryBuyer;
import com.erydevs.config.MessagesConfig;
import com.erydevs.db.Database;
import com.erydevs.papi.Placeholders;
import com.erydevs.shop.item.ShopItem;
import com.erydevs.shop.listener.ShopClickListener;
import com.erydevs.shop.type.ShopItemType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BoosterTimerTest {
    private EryBuyer plugin;
    private ShopManager shop;
    private Database database;
    private MessagesConfig messages;
    private final UUID uuid = UUID.randomUUID();
    private final AtomicLong now = new AtomicLong(1000000L);

    @Before
    public void setup() {
        plugin = mock(EryBuyer.class);
        database = mock(Database.class);
        messages = mock(MessagesConfig.class);
        when(plugin.getDataBase()).thenReturn(database);
        when(plugin.getMessagesConfig()).thenReturn(messages);
        shop = new ShopManager(plugin, now::get);
        when(plugin.getShopManager()).thenReturn(shop);
    }

    private ShopItem item(long minutes) {
        return new ShopItem("money", 22, Material.DIAMOND, "Money boost", Collections.emptyList(),
                100, ShopItemType.TEMP_MONEY_BOOST, 2, minutes, Collections.emptyList());
    }

    @Test
    public void inactiveBoosterShowsZero() {
        assertEquals("00:00", Placeholders.getBoosterTimer(plugin, uuid));
        assertFalse(shop.hasPurchasedBooster(uuid, "money"));
    }

    @Test
    public void countdownRoundsUpAndExpiresAtDeadline() {
        shop.rememberPurchasedBooster(uuid, item(30));
        assertEquals("30:00", Placeholders.getBoosterTimer(plugin, uuid));
        now.addAndGet(1001);
        assertEquals("29:59", Placeholders.getBoosterTimer(plugin, uuid));
        now.set(1000000L + 30 * 60000L - 1);
        assertEquals("00:01", Placeholders.getBoosterTimer(plugin, uuid));
        now.incrementAndGet();
        assertEquals("00:00", Placeholders.getBoosterTimer(plugin, uuid));
        assertNull(shop.getPurchasedBoosterName(uuid));
        assertFalse(shop.hasPurchasedBooster(uuid, "money"));
    }

    @Test
    public void longerDurationsKeepTotalMinutes() {
        shop.rememberPurchasedBooster(uuid, item(90));
        assertEquals("90:00", Placeholders.getBoosterTimer(plugin, uuid));
    }

    @Test
    public void duplicatePurchaseDoesNotChargeOrResetTimer() throws Exception {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(database.getTokens(uuid)).thenReturn(1000);
        ShopClickListener listener = new ShopClickListener(plugin);
        Method purchase = ShopClickListener.class.getDeclaredMethod("purchase", Player.class, ShopItem.class);
        purchase.setAccessible(true);
        purchase.invoke(listener, player, item(30));
        now.addAndGet(60000);
        purchase.invoke(listener, player, item(30));
        verify(database, times(1)).addTokens(uuid, -100);
        verify(messages).getMessageErrorBoosterBuy();
        assertEquals("29:00", Placeholders.getBoosterTimer(plugin, uuid));
        now.addAndGet(29 * 60000L);
        purchase.invoke(listener, player, item(30));
        verify(database, times(2)).addTokens(uuid, -100);
        assertEquals("30:00", Placeholders.getBoosterTimer(plugin, uuid));
    }
}
