package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundAction implements Actions {

    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.toLowerCase().startsWith("[sound]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String soundName = parseArgument(actionLine);
        if (soundName.isEmpty()) return;
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {}
    }
}
