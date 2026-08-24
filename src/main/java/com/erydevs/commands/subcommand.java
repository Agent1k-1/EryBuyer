package com.erydevs.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface subcommand {

    @NotNull
    String getName();

    @NotNull
    String getPermission();

    void execute(@NotNull Player player, @NotNull String[] args);

    @NotNull
    default List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
