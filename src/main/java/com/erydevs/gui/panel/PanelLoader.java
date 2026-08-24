package com.erydevs.gui.panel;

import com.erydevs.utils.HexUtils;
import com.erydevs.utils.head.MaterialHeadParser;
import com.erydevs.utils.head.ParsedMaterial;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PanelLoader {

    private static final String SECTION = "panels";

    private PanelLoader() {
    }

    @NotNull
    public static List<PanelConfig> load(@NotNull FileConfiguration cfg) {
        List<?> raw = cfg.getList(SECTION);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<PanelConfig> panels = new ArrayList<>();
        for (Object obj : raw) {
            if (!(obj instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            PanelConfig panel = parse((Map<String, Object>) obj);
            if (panel != null) panels.add(panel);
        }
        return panels;
    }

    @Nullable
    private static PanelConfig parse(@NotNull Map<String, Object> map) {
        String material = (String) map.get("material");
        List<?> slots = (List<?>) map.get("slots");
        if (material == null || slots == null) return null;

        ParsedMaterial pm = MaterialHeadParser.parse(material);
        if (pm == null) return null;

        String name = (String) map.get("name");
        return new PanelConfig(pm.getMaterial(), pm.getHeadTextureBase64(),
                name != null ? HexUtils.colorize(name) : null,
                readLore((List<?>) map.get("lore")),
                readSlots(slots));
    }

    @NotNull
    private static List<String> readLore(@Nullable List<?> raw) {
        List<String> lore = new ArrayList<>();
        if (raw != null) {
            for (Object line : raw) {
                String text = line.toString();
                lore.add(text.trim().isEmpty() ? " " : HexUtils.colorize(text));
            }
        }
        if (lore.isEmpty()) lore.add(" ");
        return lore;
    }

    @NotNull
    private static List<Integer> readSlots(@NotNull List<?> raw) {
        List<Integer> slots = new ArrayList<>();
        for (Object slot : raw) {
            if (slot instanceof Number) slots.add(((Number) slot).intValue());
        }
        return slots;
    }
}
