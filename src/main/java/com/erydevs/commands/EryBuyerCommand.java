package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.placeholders.PlaceholderAPIHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EryBuyerCommand implements CommandExecutor, TabCompleter {

    private final EryBuyer plugin;

    public EryBuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("erybuyer.admin")) {
            Player p = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(PlaceholderAPIHook.apply(plugin.getConfigManager().getMessageNoPermission(), p));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            Player p = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(PlaceholderAPIHook.apply(plugin.getConfigManager().getConfigReloadMessage(), p));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("erybuyer.admin")) return Collections.emptyList();
        if (args.length == 1) return Arrays.asList("reload");
        return Collections.emptyList();
    }
}