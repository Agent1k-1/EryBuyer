package com.erydevs.api.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerSellEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Material material;
    private final int amount;
    private final double price;
    private final long pointsEarned;

    public PlayerSellEvent(@NotNull Player player, @NotNull Material material, int amount, double price, long pointsEarned) {
        this.player = player;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.pointsEarned = pointsEarned;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public long getPointsEarned() {
        return pointsEarned;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
