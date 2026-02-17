package com.erydevs.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;

public class BuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public BuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("talebuyer.open")) {
            String msg = plugin.getConfigManager().getConfig().getString("message.no-permission");
            player.sendMessage(HexUtils.colorize(msg));
            return true;
        }

        player.openInventory(plugin.getBuyerGUI().createInventory(player));
        playMenuOpenSound(player);
        return true;
    }

    private void playMenuOpenSound(Player player) {
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sound.sound_open_menu"));
            player.playSound(player.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {
        }
    }
}