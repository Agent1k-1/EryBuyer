package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.placeholders.BuyerPlaceholder;
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

        if (!p.hasPermission("talebuyer.autobuyer")) {
            String msg = plugin.getConfigManager().getConfig().getString("message.no-permission");
            p.sendMessage(HexUtils.colorize(msg));
            return true;
        }

        this.plugin.getAutoBuyerManager().toggleAutobuyer(p);
        boolean enabled = this.plugin.getAutoBuyerManager().isAutobuyerEnabled(p);
        String path = enabled ? "message.autosell-on" : "message.autosell-off";
        String raw = this.plugin.getConfigManager().getConfig().getString(path);
        p.sendMessage(BuyerPlaceholder.apply(raw, p));
        return true;
    }
}