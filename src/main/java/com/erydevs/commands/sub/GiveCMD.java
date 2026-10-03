package com.erydevs.commands.sub;

import com.erydevs.EryBuyer;
import com.erydevs.commands.AdbuyerCommand;
import com.erydevs.commands.subcommand;
import com.erydevs.papi.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GiveCMD implements subcommand {

    private final EryBuyer plugin;
    private final AdbuyerCommand root;

    public GiveCMD(@NotNull EryBuyer plugin, @NotNull AdbuyerCommand root) {
        this.plugin = plugin;
        this.root = root;
    }

    @Override
    public @NotNull String getName() {
        return "tokens";
    }

    @Override
    public @NotNull String getPermission() {
        return "erybuyer.adbuyer";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("give")) {
            root.send(player, plugin.getMessagesConfig().getMessageAdminBuyer());
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            root.send(player, plugin.getMessagesConfig().getMessageAdminBuyer());
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            root.send(player, plugin.getMessagesConfig().getMessageAdminBuyer());
            return;
        }

        plugin.getDataBase().addTokens(target.getUniqueId(), amount);

        List<String> lines = plugin.getMessagesConfig().getMessageTokenGive().stream()
                .map(line -> line.replace("%buyer_token_amount%", String.valueOf(amount)))
                .map(line -> Placeholders.apply(line, target))
                .collect(Collectors.toList());
        root.send(target, lines);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return "give".startsWith(args[1].toLowerCase(Locale.ROOT)) ? Collections.singletonList("give") : Collections.emptyList();
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("give")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    names.add(online.getName());
                }
            }
            return names;
        }

        return Collections.emptyList();
    }
}
