package com.erydevs.db;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.boosters.PlayerBooster;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlDatabase implements Database {

    private final EryBuyer plugin;
    private final Logger logger;
    private final File file;
    private final YamlConfiguration data;
    private final Map<UUID, PlayerBooster> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> limitCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tokensCache = new ConcurrentHashMap<>();

    public YamlDatabase(@NotNull EryBuyer plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, fileName);
        this.data = YamlConfiguration.loadConfiguration(file);
        loadAll();
        logger.info("База данных YAML подключена: " + fileName);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    private void loadAll() {
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) return;
        for (String key : players.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            ConfigurationSection section = players.getConfigurationSection(key);
            if (section == null) continue;
            cache.put(uuid, new PlayerBooster(uuid, section.getInt("booster-level"), section.getLong("total-points")));
            tokensCache.put(uuid, section.getInt("tokens"));
            ConfigurationSection limits = section.getConfigurationSection("limits");
            if (limits == null) continue;
            for (String material : limits.getKeys(false)) {
                limitCache.put(uuid + ":" + material, limits.getInt(material));
            }
        }
    }

    @Override
    public int getSoldAmount(@NotNull UUID uuid, @NotNull String material) {
        return limitCache.getOrDefault(uuid + ":" + material, 0);
    }

    @Override
    public void addSoldAmount(@NotNull UUID uuid, @NotNull String material, int amount) {
        String key = uuid + ":" + material;
        limitCache.put(key, Math.max(0, limitCache.getOrDefault(key, 0) + amount));
        save();
    }

    @Override
    public void resetAllLimits() {
        limitCache.clear();
        save();
    }

    @Override
    public void resetLimitsForMaterial(@NotNull String material) {
        String suffix = ":" + material;
        limitCache.keySet().removeIf(key -> key.endsWith(suffix));
        save();
    }

    @Override
    @NotNull
    public PlayerBooster getPlayerData(@NotNull UUID uuid) {
        PlayerBooster booster = cache.get(uuid);
        if (booster == null) {
            booster = new PlayerBooster(uuid, 0, 0L);
            cache.put(uuid, booster);
        }
        return booster;
    }

    @Override
    public void save(@NotNull PlayerBooster booster) {
        cache.put(booster.getUuid(), booster);
        save();
    }

    @Override
    public void evictPlayer(@NotNull UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    @NotNull
    public List<Map.Entry<String, Long>> getTopPoints() {
        List<Map.Entry<String, Long>> result = new ArrayList<>();
        for (PlayerBooster booster : cache.values()) {
            if (booster.getTotalPoints() > 0) result.add(new AbstractMap.SimpleEntry<>(booster.getUuid().toString(), booster.getTotalPoints()));
        }
        result.sort(Comparator.comparingLong(Map.Entry<String, Long>::getValue).reversed());
        if (result.size() > 100) return result.subList(0, 100);
        return result;
    }

    @Override
    public void refreshTopPointsCache() {
    }

    @Override
    public int getTokens(@NotNull UUID uuid) {
        return tokensCache.getOrDefault(uuid, 0);
    }

    @Override
    public void addTokens(@NotNull UUID uuid, int amount) {
        tokensCache.put(uuid, Math.max(0, tokensCache.getOrDefault(uuid, 0) + amount));
        save();
    }

    @Override
    public void closeConnection() {
        save();
    }

    private void save() {
        for (PlayerBooster booster : cache.values()) {
            String key = "players." + booster.getUuid();
            data.set(key + ".booster-level", booster.getCurrentLevel());
            data.set(key + ".total-points", booster.getTotalPoints());
            data.set(key + ".tokens", tokensCache.getOrDefault(booster.getUuid(), 0));
        }
        for (Map.Entry<String, Integer> entry : limitCache.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            if (parts.length != 2) continue;
            data.set("players." + parts[0] + ".limits." + parts[1], entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка сохранения базы данных YAML", e);
        }
    }
}
