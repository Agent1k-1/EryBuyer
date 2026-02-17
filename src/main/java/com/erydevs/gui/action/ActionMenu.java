package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.List;

public class ActionMenu {

    public static void execute(EryBuyer plugin, Player player, List<String> actions) {
        if (actions == null) return;
        for (String raw : actions) {
            if (raw == null) continue;
            String s = raw.trim();
            String lower = s.toLowerCase();
            if (lower.equalsIgnoreCase("[close]")) {
                player.closeInventory();
                continue;
            }
            if (lower.startsWith("[openmenu]")) {
                String[] parts = s.split("\\s+", 2);
                String name = parts.length > 1 ? parts[1].trim() : "menu";
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.openInventory(plugin.getBuyerGUI().createInventory(player, name))
                );
                continue;
            }
            if (lower.startsWith("[command]")) {
                String[] parts = s.split("\\s+", 2);
                String command = parts.length > 1 ? parts[1].trim() : "";
                if (!command.isEmpty()) {
                    plugin.getServer().dispatchCommand(player, command);
                    String menuTitle = player.getOpenInventory().getTitle();
                    String menuName = plugin.getBuyerGUI().getMenuNameByTitle(menuTitle);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            player.openInventory(plugin.getBuyerGUI().createInventory(player, menuName)), 1L);
                }
                continue;
            }
            if (lower.startsWith("[sound]")) {
                String[] parts = s.split("\\s+", 2);
                String soundName = parts.length > 1 ? parts[1].trim() : "";
                if (!soundName.isEmpty()) {
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (Exception ignored) {}
                }
                continue;
            }
        }
    }
}
