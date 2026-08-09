package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public BuyerCommand(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!player.hasPermission("erybuyer.open")) {
            ActionType.dispatchAll(plugin, player,plugin.getMessagesConfig().getMessageNoPermission());
            return true;
        }

        player.openInventory(plugin.getBuyerGUI().createInventory(player));
        playMenuOpenSound(player);
        return true;
    }

    private void playMenuOpenSound(@NotNull Player player) {
        if (!plugin.getConfigManager().isSoundOpenMenuEnabled()) return;

        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundOpenMenu());
            float volume = plugin.getConfigManager().getSoundOpenMenuVolume();
            float pitch = plugin.getConfigManager().getSoundOpenMenuPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }
}