package com.erydevs.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import com.erydevs.EryBuyer;

public class PlayerQuitListener implements Listener {

    private final EryBuyer plugin;

    public PlayerQuitListener(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        plugin.getAutoBuyerManager().removePlayer(e.getPlayer());
    }
}