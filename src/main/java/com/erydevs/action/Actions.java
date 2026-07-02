package com.erydevs.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;

import java.util.List;

public interface Actions {

    List<Actions> HANDLERS = List.of(
            new CloseAction(),
            new OpenMenuAction(),
            new CommandAction(),
            new SoundAction(),
            new MessageAction()
    );

    boolean canHandle(String actionLine);

    void execute(String actionLine, EryBuyer plugin, Player player);

    default String parseArgument(String actionLine) {
        String[] parts = actionLine.split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    static void dispatch(EryBuyer plugin, Player player, List<String> actions) {
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
