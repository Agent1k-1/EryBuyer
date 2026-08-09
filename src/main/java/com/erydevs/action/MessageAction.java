package com.erydevs.action;

import com.erydevs.EryBuyer;
import com.erydevs.utils.HexUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageAction implements Action {

    @Override
    public void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player) {
        String text = parseArgument(actionLine);
        if (text.isEmpty()) return;
        player.sendMessage(HexUtils.colorize(text));
    }
}
