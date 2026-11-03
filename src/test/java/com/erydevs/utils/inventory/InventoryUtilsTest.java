package com.erydevs.utils.inventory;

import com.erydevs.gui.entry.Entry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InventoryUtilsTest {
    @Test
    public void countAndRemoveOnlyMatchingModel() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        ItemStack plain = stack(null, 3);
        ItemStack matching = stack(1001, 4);
        ItemStack other = stack(1002, 5);
        when(inventory.getContents()).thenReturn(new ItemStack[]{plain, matching, other});
        Entry entry = entry(1001);
        assertEquals(4, InventoryUtils.count(player, entry));
        assertEquals(4, InventoryUtils.remove(player, entry, 64));
        verify(inventory).setItem(1, null);
        verify(inventory, never()).setItem(eq(0), any());
        verify(inventory, never()).setItem(eq(2), any());
    }

    @Test
    public void unspecifiedModelKeepsMaterialMatching() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        ItemStack plain = stack(null, 3);
        ItemStack custom = stack(1001, 4);
        when(inventory.getContents()).thenReturn(new ItemStack[]{plain, custom});
        assertEquals(7, InventoryUtils.count(player, entry(null)));
        assertEquals(7, InventoryUtils.count(player, Material.DIAMOND));
    }

    @Test
    public void zeroModelKeepsMaterialMatching() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContents()).thenReturn(new ItemStack[]{stack(null, 3), stack(0, 4)});
        assertEquals(7, InventoryUtils.count(player, entry(0)));
    }

    private Entry entry(Integer model) {
        return new Entry("token", Material.DIAMOND, null, "token", Collections.emptyList(), 1, 64, 0, 0, 0, null, model);
    }

    private ItemStack stack(Integer model, int amount) {
        ItemStack stack = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        when(stack.getType()).thenReturn(Material.DIAMOND);
        when(stack.getAmount()).thenReturn(amount);
        when(stack.getItemMeta()).thenReturn(meta);
        when(meta.hasCustomModelData()).thenReturn(model != null);
        if (model != null) when(meta.getCustomModelData()).thenReturn(model);
        return stack;
    }
}
