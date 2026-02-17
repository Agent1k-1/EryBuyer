package com.erydevs.gui;

public class BuyerSite {

    public enum ClickType {
        LEFT,
        RIGHT
    }

    private final String itemId;
    private final ClickType clickType;
    private final int amount;
    private final double price;

    public BuyerSite(String itemId, ClickType clickType, int amount, double price) {
        this.itemId = itemId;
        this.clickType = clickType;
        this.amount = amount;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public static BuyerSite fromLeftClick(String itemId, int amount, double price) {
        return new BuyerSite(itemId, ClickType.LEFT, amount, price);
    }

    public static BuyerSite fromRightClick(String itemId, int amount, double price) {
        return new BuyerSite(itemId, ClickType.RIGHT, amount, price);
    }
}