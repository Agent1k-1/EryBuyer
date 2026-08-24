package com.erydevs.commands.tab;

import com.erydevs.commands.AdbuyerCommand;
import com.erydevs.commands.subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AdbuyerTabCompleter implements TabCompleter {

    private final AdbuyerCommand root;

    public AdbuyerTabCompleter(@NotNull AdbuyerCommand root) {
        this.root = root;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(AdbuyerCommand.PERMISSION)) return Collections.emptyList();

        if (args.length <= 1) {
            return completeSubcommands(sender, args.length == 0 ? "" : args[0]);
        }

        subcommand sub = root.getSubcommand(args[0]);
        if (sub == null || !sender.hasPermission(sub.getPermission())) return Collections.emptyList();

        return sub.tabComplete(sender, args);
    }

    @NotNull
    private List<String> completeSubcommands(@NotNull CommandSender sender, @NotNull String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);

        List<String> names = new ArrayList<>();
        for (subcommand sub : root.getSubcommands()) {
            if (!sender.hasPermission(sub.getPermission())) continue;
            if (!sub.getName().toLowerCase(Locale.ROOT).startsWith(lower)) continue;
            names.add(sub.getName());
        }
        Collections.sort(names);
        return names;
    }
}
