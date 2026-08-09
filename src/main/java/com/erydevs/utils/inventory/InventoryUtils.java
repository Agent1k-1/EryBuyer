package com.erydevs.utils.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    public static int count(@NotNull Player player, @NotNull Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) total += stack.getAmount();
        }
        return total;
    }

    public static int remove(@NotNull Player player, @NotNull Material material, int amount) {
        if (amount <= 0) return 0;

        int removed = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && removed < amount; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) continue;

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
}
