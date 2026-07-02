package com.erydevs.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.erydevs.EryBuyer;

public class PlayerQuitListener implements Listener {

    private final EryBuyer plugin;

    public PlayerQuitListener(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getAutoBuyerManager().removePlayer(e.getPlayer());
    }
}