package com.erydevs.commands.admin;

import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminBuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public AdminBuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("erybuyer.admin")) {
            String msg = plugin.getConfigManager().getMessageNoPermission();
            player.sendMessage(HexUtils.colorize(msg));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("reload")) {
            reloadConfig(player);
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void reloadConfig(Player player) {
        try {
            plugin.getConfigManager().reloadConfig();
            
            if (plugin.getLevelConfig() != null) {
                plugin.getLevelConfig().reloadLevels();
            }
            
            if (plugin.getBuyerGUI() != null) {
                plugin.getBuyerGUI().reloadMenus();
            }
            
            String msg = plugin.getConfigManager().getConfigReloadMessage();
            player.sendMessage(HexUtils.colorize(msg));
        } catch (Exception e) {
            player.sendMessage(HexUtils.colorize("&cОшибка при перезагрузке конфига: " + e.getMessage()));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(HexUtils.colorize("&c▪ &fКоманды администратора:"));
        player.sendMessage(HexUtils.colorize("&c▪ &f/adminbuyer reload &7- перезагрузить конфиг"));
    }
}
