package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.papi.PlaceholderAPIHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EryBuyerCommand implements CommandExecutor, TabCompleter {

    private final EryBuyer plugin;

    public EryBuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission("erybuyer.admin")) {
            if (p != null) Actions.dispatch(plugin, p, plugin.getConfigManager().getMessageNoPermission());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            if (p != null) {
                List<String> lines = plugin.getConfigManager().getConfigReloadMessage().stream()
                        .map(line -> PlaceholderAPIHook.apply(line, p))
                        .collect(Collectors.toList());
                Actions.dispatch(plugin, p, lines);
            }
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
