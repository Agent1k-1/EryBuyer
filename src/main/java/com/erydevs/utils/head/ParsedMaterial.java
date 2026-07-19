package com.erydevs.utils.head;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ParsedMaterial {

    private final Material material;
    private final String headTextureBase64;

    private ParsedMaterial(@NotNull Material material, @Nullable String headTextureBase64) {
        this.material = Objects.requireNonNull(material, "material");
        this.headTextureBase64 = headTextureBase64;
    }

    @NotNull
    public static ParsedMaterial plain(@NotNull Material material) {
        return new ParsedMaterial(material, null);
    }

    @NotNull
    public static ParsedMaterial playerHead(@NotNull String texturePropertyValue) {
        return new ParsedMaterial(Material.PLAYER_HEAD, Objects.requireNonNull(texturePropertyValue));
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public String getHeadTextureBase64() {
        return headTextureBase64;
    }

    public boolean isCustomHead() {
        return headTextureBase64 != null;
    }
}
