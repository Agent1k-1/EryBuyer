package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.placeholders.PlaceholderAPIHook;
import com.erydevs.utils.HexUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoBuyerCommand implements CommandExecutor {
    private final EryBuyer plugin;

    public AutoBuyerCommand(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("erybuyer.autobuyer")) {
            String msg = plugin.getConfigManager().getMessageNoPermission();
            p.sendMessage(HexUtils.colorize(msg));
            return true;
        }

        this.plugin.getAutoBuyerManager().toggleAutobuyer(p);
        boolean enabled = this.plugin.getAutoBuyerManager().isAutobuyerEnabled(p);
        String raw = enabled ? plugin.getConfigManager().getMessageAutoBuyerOn() : plugin.getConfigManager().getMessageAutoBuyerOff();
        p.sendMessage(PlaceholderAPIHook.apply(raw, p));
        return true;
    }
}