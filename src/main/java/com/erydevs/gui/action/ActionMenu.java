package com.erydevs.gui.action;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ActionMenu {
    
    private static final List<ActionHandler> handlers = new ArrayList<>();
    
    static {
        handlers.add(new CloseAction());
        handlers.add(new OpenMenuAction());
        handlers.add(new CommandAction());
        handlers.add(new SoundAction());
    }

    public static void execute(EryBuyer plugin, Player player, List<String> actions) {
        if (actions == null) return;
        for (String action : actions) {
            if (action == null) continue;
            String trimmed = action.trim();
            if (trimmed.isEmpty()) continue;
            
            for (ActionHandler handler : handlers) {
                if (handler.canHandle(trimmed)) {
                    handler.execute(trimmed, plugin, player);
                    break;
                }
            }
        }
    }
}

