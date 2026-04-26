package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundAction implements ActionHandler {
    
    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.trim().toLowerCase().startsWith("[sound]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String[] parts = actionLine.split("\\s+", 2);
        String soundName = parts.length > 1 ? parts[1].trim() : "";
        if (!soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }
}
