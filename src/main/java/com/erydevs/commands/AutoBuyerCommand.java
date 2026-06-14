package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.papi.PlaceholderAPIHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class AutoBuyerCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public AutoBuyerCommand(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("erybuyer.autobuyer")) {
            Actions.dispatch(plugin, player, plugin.getMessagesConfig().getMessageNoPermission());
            return true;
        }

        plugin.getAutoBuyerManager().toggleAutobuyer(player);

        List<String> lines = plugin.getMessagesConfig().getMessageAutoBuyerStatus().stream()
                .map(line -> PlaceholderAPIHook.apply(line, player))
                .collect(Collectors.toList());
        Actions.dispatch(plugin, player, lines);
        return true;
    }
}