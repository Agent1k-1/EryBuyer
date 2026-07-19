package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Actions {

    List<Actions> HANDLERS = List.of(
            new CloseAction(),
            new OpenMenuAction(),
            new CommandAction(),
            new SoundAction(),
            new MessageAction()
    );

    boolean canHandle(@NotNull String actionLine);

    void execute(@NotNull String actionLine, @NotNull EryBuyer plugin, @NotNull Player player);

    @NotNull
    default String parseArgument(@NotNull String actionLine) {
        String[] parts = actionLine.split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    static void dispatch(@NotNull EryBuyer plugin, @NotNull Player player, @Nullable List<String> actions) {
        if (actions == null) return;
        for (String raw : actions) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty()) continue;
            HANDLERS.stream()
                    .filter(h -> h.canHandle(line))
                    .findFirst()
                    .ifPresent(h -> h.execute(line, plugin, player));
        }
    }
}
