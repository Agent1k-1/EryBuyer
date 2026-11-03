package com.erydevs.utils.inventory;

import org.bukkit.Material;
import com.erydevs.gui.entry.Entry;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    public static int count(@NotNull Player player, @NotNull Material material) {
        return count(player, material, null);
    }

    public static int count(@NotNull Player player, @NotNull Entry entry) {
        return count(player, entry.material, entry.customModelData);
    }

    private static int count(Player player, Material material, Integer customModelData) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (matches(stack, material, customModelData)) total += stack.getAmount();
        }
        return total;
    }

    public static int remove(@NotNull Player player, @NotNull Material material, int amount) {
        return remove(player, material, null, amount);
    }

    public static int remove(@NotNull Player player, @NotNull Entry entry, int amount) {
        return remove(player, entry.material, entry.customModelData, amount);
    }

    private static int remove(Player player, Material material, Integer customModelData, int amount) {
        if (amount <= 0) return 0;

        int removed = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && removed < amount; i++) {
            ItemStack stack = contents[i];
            if (!matches(stack, material, customModelData)) continue;

            int canRemove = Math.min(stack.getAmount(), amount - removed);
            if (stack.getAmount() > canRemove) {
                stack.setAmount(stack.getAmount() - canRemove);
                player.getInventory().setItem(i, stack);
            } else {
                player.getInventory().setItem(i, null);
            }
            removed += canRemove;
        }
        return removed;
    }
    private static boolean matches(@Nullable ItemStack stack, Material material, @Nullable Integer customModelData) {
        if (stack == null || stack.getType() != material) return false;
        if (customModelData == null) return true;
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData;
    }

}
