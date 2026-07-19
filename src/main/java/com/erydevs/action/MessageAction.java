package com.erydevs.action;

import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageAction implements Actions {

    @Override
    public boolean canHandle(@NotNull String actionLine) {
        return actionLine.toLowerCase().startsWith("[message]");
    }

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        String text = parseArgument(actionLine);
        if (text.isEmpty()) return;
        player.sendMessage(HexUtils.colorize(text));
    }
}
