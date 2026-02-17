package com.erydevs.commands;

import com.erydevs.EryBuyer;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.utils.HexUtils;
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

        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double requiredForNext = plugin.getLevelConfig().getRequiredMoneyForLevel(playerLevel.getCurrentLevel() + 1);
        double remaining = Math.max(0, requiredForNext - playerLevel.getTotalEarned());

        for (String line : plugin.getConfigManager().getConfig().getStringList("message.level-info")) {
            String processed = line.replace("%buyer_total_earned%", String.valueOf((long) playerLevel.getTotalEarned()))
                    .replace("%buyer_current_level%", String.valueOf(playerLevel.getCurrentLevel()))
                    .replace("%buyer_required_amount%", String.valueOf((long) remaining));
            p.sendMessage(HexUtils.colorize(processed));
        }

        return true;
    }
}
