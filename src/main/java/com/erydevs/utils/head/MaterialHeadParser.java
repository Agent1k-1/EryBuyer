package com.erydevs.utils.head;

import org.bukkit.Material;

public final class MaterialHeadParser {

    private static final String BASEHEAD_PREFIX = "basehead-";
    private static final String TEXTURE_PREFIX = "texture-";

    private MaterialHeadParser() {
    }

    public static com.erydevs.utils.head.ParsedMaterial parse(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.length() > BASEHEAD_PREFIX.length()
                && s.regionMatches(true, 0, BASEHEAD_PREFIX, 0, BASEHEAD_PREFIX.length())) {
            String payload = s.substring(BASEHEAD_PREFIX.length());
            if (payload.isEmpty()) {
                return null;
            }
            return ParsedMaterial.playerHead(payload);
        }
        if (s.length() > TEXTURE_PREFIX.length()
                && s.regionMatches(true, 0, TEXTURE_PREFIX, 0, TEXTURE_PREFIX.length())) {
            String hash = s.substring(TEXTURE_PREFIX.length());
            if (hash.isEmpty()) {
                return null;
            }
            return ParsedMaterial.playerHead(SkullUtils.getEncoded(hash));
        }
        Material m = Material.matchMaterial(s);
        if (m == null) {
            return null;
        }
        return ParsedMaterial.plain(m);
    }
}
