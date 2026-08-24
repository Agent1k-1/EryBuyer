package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdbuyerCommand implements CommandExecutor {

    public static final String PERMISSION = "erybuyer.adbuyer";

    private final EryBuyer plugin;
    private final Map<String, subcommand> subcommands = new HashMap<>();

    public AdbuyerCommand(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void register(@NotNull subcommand sub) {
        subcommands.put(sub.getName().toLowerCase(Locale.ROOT), sub);
    }

    @NotNull
    public Collection<subcommand> getSubcommands() {
        return subcommands.values();
    }

    @Nullable
    public subcommand getSubcommand(@NotNull String name) {
        return subcommands.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission(PERMISSION)) {
            send(player, plugin.getMessagesConfig().getMessageNoPermission());
            return true;
        }

        subcommand sub = args.length > 0 ? getSubcommand(args[0]) : null;
        if (sub == null) {
            send(player, plugin.getMessagesConfig().getMessageAdminBuyer());
            return true;
        }

        if (!player.hasPermission(sub.getPermission())) {
            send(player, plugin.getMessagesConfig().getMessageNoPermission());
            return true;
        }

        sub.execute(player, args);
        return true;
    }

    public void send(@NotNull Player player, @NotNull List<String> lines) {
        ActionType.dispatchAll(plugin, player, lines);
    }
}
