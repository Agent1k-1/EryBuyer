package com.erydevs.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class PlaceholderApiBridge {

    private PlaceholderApiBridge() {
    }

    @NotNull
    static String apply(@NotNull Player player, @NotNull String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
