package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public BuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!player.hasPermission("erybuyer.open")) {
            Actions.dispatch(plugin, player, plugin.getConfigManager().getMessageNoPermission());
            return true;
        }

        player.openInventory(plugin.getBuyerGUI().createInventory(player));
        playMenuOpenSound(player);
        return true;
    }

    private void playMenuOpenSound(Player player) {
        if (!plugin.getConfigManager().isSoundOpenMenuEnabled()) return;
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundOpenMenu());
            float volume = plugin.getConfigManager().getSoundOpenMenuVolume();
            float pitch = plugin.getConfigManager().getSoundOpenMenuPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception ignored) {}
    }
}
