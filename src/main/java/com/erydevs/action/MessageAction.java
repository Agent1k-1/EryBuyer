package com.erydevs.action;

import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import org.bukkit.entity.Player;

public class MessageAction implements Actions {

    @Override
    public boolean canHandle(String actionLine) {
        return actionLine.toLowerCase().startsWith("[message]");
    }

    @Override
    public void execute(String actionLine, EryBuyer plugin, Player player) {
        String text = parseArgument(actionLine);
        if (text.isEmpty()) return;
        player.sendMessage(HexUtils.colorize(text));
    }
}
