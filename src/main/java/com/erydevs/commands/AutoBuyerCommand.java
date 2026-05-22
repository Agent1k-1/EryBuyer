package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.papi.PlaceholderAPIHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class AutoBuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public AutoBuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (!p.hasPermission("erybuyer.autobuyer")) {
            Actions.dispatch(plugin, p, plugin.getConfigManager().getMessageNoPermission());
            return true;
        }

        plugin.getAutoBuyerManager().toggleAutobuyer(p);
        List<String> lines = plugin.getConfigManager().getMessageAutoBuyerStatus().stream()
                .map(line -> PlaceholderAPIHook.apply(line, p))
                .collect(Collectors.toList());
        Actions.dispatch(plugin, p, lines);
        return true;
    }
}
