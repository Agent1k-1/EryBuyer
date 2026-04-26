package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

public interface ActionHandler {
    boolean canHandle(String actionLine);
    void execute(String actionLine, EryBuyer plugin, Player player);
}
