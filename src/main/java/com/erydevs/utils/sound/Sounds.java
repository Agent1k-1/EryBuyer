package com.erydevs.utils.sound;

import com.erydevs.EryBuyer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {

    private final EryBuyer plugin;

    public Sounds(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void playMenuOpenSound(Player player) {
        if (!plugin.getConfigManager().isSoundOpenMenuEnabled()) return;
        try {
            String soundName = plugin.getConfigManager().getSoundOpenMenu();
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public void playNoItemSound(Player player) {
        if (!plugin.getConfigManager().isSoundNoItemEnabled()) return;
        try {
            String soundName = plugin.getConfigManager().getSoundNoItem();
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public void playAutobuyerSound(Player player) {
        if (!plugin.getConfigManager().isSoundAutobuyerEnabled()) return;
        try {
            String soundName = plugin.getConfigManager().getSoundAutobuyer();
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }
}
