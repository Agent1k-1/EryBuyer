package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum ActionType {

    CLOSE("[close]", new CloseAction()),
    COMMAND("[command]", new CommandAction()),
    OPENMENU("[openmenu]", new OpenMenuAction()),
    SOUND("[sound]", new SoundAction()),
    MESSAGE("[message]", new MessageAction());

    private final String prefix;
    private final Action action;

    ActionType(@NotNull String prefix, @NotNull Action action) {
        this.prefix = prefix;
        this.action = action;
    }

    public static void dispatch(@NotNull EryBuyer plugin, @NotNull Player player, @Nullable String actionLine) {
        if (actionLine == null) return;
        String line = actionLine.trim();
        if (line.isEmpty()) return;

        String lower = line.toLowerCase();
        for (ActionType type : values()) {
            if (lower.startsWith(type.prefix)) {
                type.action.execute(line, plugin, player);
                return;
            }
        }
    }

    public static void dispatchAll(@NotNull EryBuyer plugin, @NotNull Player player, @Nullable List<String> actions) {
        if (actions == null) return;
        for (String raw : actions) {
            dispatch(plugin, player, raw);
        }
    }
}
