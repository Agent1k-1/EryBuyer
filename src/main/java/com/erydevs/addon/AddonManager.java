package com.erydevs.addon;

import com.erydevs.EryBuyer;
import com.erydevs.addon.loader.AddonLoader;
import com.erydevs.api.EryBuyerAPI;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddonManager {

    private final EryBuyer plugin;
    private final AddonLoader loader;
    private final List<Addon> addons = new ArrayList<>();

    public AddonManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.loader = new AddonLoader(plugin);
    }

    public void enable() {
        File folder = new File(plugin.getDataFolder(), "addons");
        List<Addon> loaded = loader.loadAll(folder);

        for (Addon addon : loaded) {
            try {
                addon.onEnable(plugin);
                addons.add(addon);
                EryBuyerAPI.getInstance().registerAddon(addon);
                plugin.getLogger().info("Аддон загружен: " + addon.getName() + " v" + addon.getVersion());
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка включения аддона " + addon.getName() + ": " + e.getMessage());
            }
        }
    }

    public void disable() {
        for (Addon addon : addons) {
            try {
                addon.onDisable();
                EryBuyerAPI.getInstance().unregisterAddon(addon.getName());
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка отключения аддона " + addon.getName() + ": " + e.getMessage());
            }
        }
        addons.clear();
    }

    @NotNull
    public List<Addon> getAddons() {
        return addons;
    }
}
