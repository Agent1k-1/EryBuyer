package com.erydevs.utils.head;

import com.erydevs.EryBuyer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

public final class SkullUtils {

    private static final Pattern SKIN_URL_IN_JSON = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

    private static final Map<String, ItemStack> CACHE = new ConcurrentHashMap<>();

    private static volatile Boolean playerProfileApiPresent;

    private SkullUtils() {
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static String getEncoded(String textureHash) {
        byte[] encoded = Base64.getEncoder().encode(String
                .format("{textures:{SKIN:{url:\"%s\"}}}", "https://textures.minecraft.net/texture/" + textureHash)
                .getBytes(StandardCharsets.UTF_8));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static ItemStack getSkullByBase64(EryBuyer plugin, String base64TextureValue) {
        if (base64TextureValue == null || base64TextureValue.isEmpty()) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        try {
            return CACHE.computeIfAbsent(base64TextureValue, k -> buildSkull(plugin, k)).clone();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to build custom skull", e);
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    private static ItemStack buildSkull(EryBuyer plugin, String base64TextureValue) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }
        if (isPlayerProfileApiPresent()) {
            if (applyPlayerProfileSkullReflect(meta, base64TextureValue, plugin)) {
                head.setItemMeta(meta);
                return head;
            }
            head.setItemMeta(meta);
            return head;
        }
        applyLegacyGameProfileSkull(meta, base64TextureValue, plugin);
        head.setItemMeta(meta);
        return head;
    }

    private static boolean isPlayerProfileApiPresent() {
        Boolean cached = playerProfileApiPresent;
        if (cached != null) {
            return cached;
        }
        synchronized (SkullUtils.class) {
            if (playerProfileApiPresent != null) {
                return playerProfileApiPresent;
            }
            try {
                Class.forName("org.bukkit.profile.PlayerProfile");
                playerProfileApiPresent = true;
            } catch (ClassNotFoundException e) {
                playerProfileApiPresent = false;
            }
            return playerProfileApiPresent;
        }
    }

    private static boolean applyPlayerProfileSkullReflect(SkullMeta meta, String base64TextureValue, EryBuyer plugin) {
        try {
            Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Object profile = createPlayerProfileReflect();
            String skinUrl = decodeSkinUrl(base64TextureValue);
            if (skinUrl == null) {
                plugin.getLogger().warning("Custom skull: could not decode texture URL from base64");
                return false;
            }
            URL url = new URL(normalizeTextureUrl(skinUrl));
            Object textures = profile.getClass().getMethod("getTextures").invoke(profile);
            invokeSetSkin(textures, url);
            invokeSetTextures(profile, textures);
            Method setOwner = SkullMeta.class.getMethod("setOwnerProfile", playerProfileClass);
            setOwner.invoke(meta, profile);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.WARNING, "Custom skull: bad skin URL", e);
            return false;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.WARNING, "Custom skull: PlayerProfile reflection failed", e);
            return false;
        }
    }

    private static void invokeSetSkin(Object textures, URL url) throws ReflectiveOperationException {
        for (Method m : textures.getClass().getMethods()) {
            if ("setSkin".equals(m.getName()) && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == URL.class) {
                m.invoke(textures, url);
                return;
            }
        }
        throw new NoSuchMethodException("setSkin(URL)");
    }

    private static void invokeSetTextures(Object profile, Object textures) throws ReflectiveOperationException {
        for (Method m : profile.getClass().getMethods()) {
            if (!"setTextures".equals(m.getName()) || m.getParameterCount() != 1) {
                continue;
            }
            Class<?> param = m.getParameterTypes()[0];
            if (param.isInstance(textures)) {
                m.invoke(profile, textures);
                return;
            }
        }
        throw new NoSuchMethodException("setTextures for " + textures.getClass().getName());
    }

    private static Object createPlayerProfileReflect() throws ReflectiveOperationException {
        for (Method m : Bukkit.class.getMethods()) {
            if (!"createPlayerProfile".equals(m.getName())) {
                continue;
            }
            Class<?>[] params = m.getParameterTypes();
            try {
                if (params.length == 2 && params[0] == UUID.class && params[1] == String.class) {
                    return m.invoke(null, UUID.randomUUID(), null);
                }
                if (params.length == 1 && params[0] == UUID.class) {
                    return m.invoke(null, UUID.randomUUID());
                }
            } catch (Exception ignored) {
                continue;
            }
        }
        throw new NoSuchMethodException("Bukkit.createPlayerProfile");
    }

    private static String normalizeTextureUrl(String skinUrl) {
        if (skinUrl.startsWith("http://textures.minecraft.net/")) {
            return "https://textures.minecraft.net/" + skinUrl.substring("http://textures.minecraft.net/".length());
        }
        return skinUrl;
    }

    private static String decodeSkinUrl(String base64Texture) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64Texture.trim()), StandardCharsets.UTF_8);
            Matcher matcher = SKIN_URL_IN_JSON.matcher(decoded);
            return matcher.find() ? matcher.group(1) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void applyLegacyGameProfileSkull(SkullMeta meta, String base64TextureValue, EryBuyer plugin) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64TextureValue));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set skull profile (legacy custom head)", e);
        }
    }
}
