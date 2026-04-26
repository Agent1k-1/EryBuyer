package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.placeholders.PlaceholderAPIHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final EryBuyer plugin;

    public LevelCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        for (String line : plugin.getConfigManager().getMessageLevelInfo()) {
            p.sendMessage(PlaceholderAPIHook.apply(line, p));
        }

        return true;
    }
}
