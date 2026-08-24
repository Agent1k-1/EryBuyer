package com.erydevs.gui.button;

import com.erydevs.utils.head.MaterialHeadParser;
import com.erydevs.utils.head.ParsedMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ButtonLoader {

    private static final String[] SECTIONS = {"menu-settings", "knops-settings"};

    private ButtonLoader() {
    }

    @NotNull
    public static Map<Integer, ButtonConfig> load(@NotNull FileConfiguration cfg) {
        Map<Integer, ButtonConfig> buttons = new HashMap<>();
        for (String section : SECTIONS) {
            loadSection(cfg, section, buttons);
        }
        return buttons;
    }

    private static void loadSection(@NotNull FileConfiguration cfg, @NotNull String section, @NotNull Map<Integer, ButtonConfig> buttons) {
        ConfigurationSection sect = cfg.getConfigurationSection(section);
        if (sect == null) return;

        for (String key : sect.getKeys(false)) {
            String path = section + "." + key;
            if (!cfg.contains(path + ".slot")) continue;

            int slot = cfg.getInt(path + ".slot");
            if (slot < 0) continue;

            ParsedMaterial pm = MaterialHeadParser.parse(cfg.getString(path + ".material"));
            if (pm == null) continue;

            buttons.put(slot, new ButtonConfig(key, slot, pm.getMaterial(), pm.getHeadTextureBase64(),
                    cfg.getString(path + ".name"),
                    cfg.getStringList(path + ".lore"),
                    readActions(cfg, path),
                    cfg.getDouble(path + ".prince-x1"),
                    cfg.getDouble(path + ".prince-x64"),
                    cfg.getInt(path + ".points-from-the-buyer-x1")));
        }
    }

    @NotNull
    private static List<String> readActions(@NotNull FileConfiguration cfg, @NotNull String path) {
        List<String> actions = cfg.getStringList(path + ".action");
        if (!actions.isEmpty()) return actions;

        String single = cfg.getString(path + ".action");
        return single != null ? Collections.singletonList(single) : Collections.emptyList();
    }

    @NotNull
    public static Optional<ButtonConfig> findByAction(@NotNull FileConfiguration cfg, @NotNull String actionFragment) {
        return load(cfg).values().stream()
                .filter(btn -> btn.hasAction(actionFragment))
                .findFirst();
    }

    public static int findSlotByAction(@NotNull FileConfiguration cfg, @NotNull String actionFragment) {
        return findByAction(cfg, actionFragment)
                .map(ButtonConfig::getSlot)
                .orElse(-1);
    }
}
