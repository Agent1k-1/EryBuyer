package com.erydevs.gui.impl.head;

import org.bukkit.Material;

import java.util.Objects;

public final class ParsedMaterial {

    private final Material material;
    private final String headTextureBase64;

    private ParsedMaterial(Material material, String headTextureBase64) {
        this.material = Objects.requireNonNull(material, "material");
        this.headTextureBase64 = headTextureBase64;
    }

    public static ParsedMaterial plain(Material material) {
        return new ParsedMaterial(material, null);
    }

    public static ParsedMaterial playerHead(String texturePropertyValue) {
        return new ParsedMaterial(Material.PLAYER_HEAD, Objects.requireNonNull(texturePropertyValue));
    }

    public Material getMaterial() {
        return material;
    }

    public String getHeadTextureBase64() {
        return headTextureBase64;
    }

    public boolean isCustomHead() {
        return headTextureBase64 != null;
    }
}
