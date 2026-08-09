package com.erydevs.data;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.boosters.PlayerBooster;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLite {

    private static final String DB_FILE = "playerdata.db";
    private static final String TABLE = "buyer_players";
    private static final String LIMITS_TABLE = "buyer_limits";

    private final EryBuyer plugin;
    private final Logger logger;
    private final File dbFile;
    private final Map<UUID, PlayerBooster> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> limitCache = new ConcurrentHashMap<>();
    private volatile List<Map.Entry<String, Long>> topPointsCache = new ArrayList<>();

    private Connection connection;

    public SQLite(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.dbFile = new File(folder, DB_FILE);

        connect();
        if (isConnected()) {
            createTable();
            refreshTopPointsCache();
        }
    }

    private void connect() {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.DELETE);
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath(), config.toProperties());
            logger.info("База данных SQLite подключена: " + DB_FILE);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка подключения к базе данных", e);
        }
    }

    private synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка проверки соединения с базой данных", e);
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTable() {
        String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "uuid TEXT PRIMARY KEY NOT NULL, " +
                "booster_level INTEGER NOT NULL DEFAULT 0, " +
                "total_points INTEGER NOT NULL DEFAULT 0)";

        String createLimits = "CREATE TABLE IF NOT EXISTS " + LIMITS_TABLE + " (" +
                "uuid TEXT NOT NULL, " +
                "material TEXT NOT NULL, " +
                "sold INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(uuid, material))";

        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate(create);
            st.executeUpdate(createLimits);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка создания таблицы базы данных", e);
        }
    }

    public int getSoldAmount(@NotNull UUID uuid, @NotNull String material) {
        String key = uuid + ":" + material;
        Integer cached = limitCache.get(key);
        if (cached != null) return cached;
        if (!isConnected()) {
            limitCache.put(key, 0);
            return 0;
        }

        String sql = "SELECT sold FROM " + LIMITS_TABLE + " WHERE uuid = ? AND material = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setString(2, material);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt("sold");
                    limitCache.put(key, value);
                    return value;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка загрузки лимита " + uuid + ":" + material, e);
        }
        limitCache.put(key, 0);
        return 0;
    }

    public void addSoldAmount(@NotNull UUID uuid, @NotNull String material, int amount) {
        String key = uuid + ":" + material;
        int current = getSoldAmount(uuid, material);
        int updated = current + amount;
        limitCache.put(key, updated);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> writeSoldAmount(uuid, material, updated));
    }

    private void writeSoldAmount(@NotNull UUID uuid, @NotNull String material, int value) {
        if (!isConnected()) return;

        String sql = "INSERT INTO " + LIMITS_TABLE + " (uuid, material, sold) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid, material) DO UPDATE SET sold = excluded.sold";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setString(2, material);
            st.setInt(3, value);
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сохранения лимита " + uuid + ":" + material, e);
        }
    }

    public void resetAllLimits() {
        limitCache.clear();
        if (!isConnected()) return;

        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM " + LIMITS_TABLE);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сброса лимитов", e);
        }
    }

    public void resetLimitsForMaterial(@NotNull String material) {
        limitCache.entrySet().removeIf(e -> e.getKey().endsWith(":" + material));
        if (!isConnected()) return;

        String sql = "DELETE FROM " + LIMITS_TABLE + " WHERE material = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, material);
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сброса лимита для " + material, e);
        }
    }

    @NotNull
    public PlayerBooster getPlayerData(@NotNull UUID uuid) {
        PlayerBooster cached = cache.get(uuid);
        if (cached != null) return cached;
        if (!isConnected()) return cacheDefault(uuid);

        String sql = "SELECT booster_level, total_points FROM " + TABLE + " WHERE uuid = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    PlayerBooster pb = new PlayerBooster(uuid, rs.getInt("booster_level"), rs.getLong("total_points"));
                    cache.put(uuid, pb);
                    return pb;
                }

            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка загрузки данных игрока " + uuid, e);
        }
        return cacheDefault(uuid);
    }

    @NotNull
    private PlayerBooster cacheDefault(@NotNull UUID uuid) {
        PlayerBooster pb = new PlayerBooster(uuid, 0, 0L);
        cache.put(uuid, pb);
        return pb;
    }

    public void save(@NotNull PlayerBooster booster) {
        cache.put(booster.getUuid(), booster);
        if (!isConnected()) return;

        String sql = "INSERT INTO " + TABLE + " (uuid, booster_level, total_points) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET booster_level = excluded.booster_level, total_points = excluded.total_points";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, booster.getUuid().toString());
            st.setInt(2, booster.getCurrentLevel());
            st.setLong(3, booster.getTotalPoints());
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сохранения данных игрока " + booster.getUuid(), e);
        }
    }

    public void evictPlayer(@NotNull UUID uuid) {
        PlayerBooster booster = cache.remove(uuid);
        if (booster != null) save(booster);
    }

    @NotNull
    public List<Map.Entry<String, Long>> getTopPoints() {
        return topPointsCache;
    }

    public void refreshTopPointsCache() {
        if (!isConnected()) return;

        String sql = "SELECT uuid, total_points FROM " + TABLE +
                " WHERE total_points > 0 ORDER BY total_points DESC LIMIT 100";
        List<Map.Entry<String, Long>> result = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new AbstractMap.SimpleEntry<>(rs.getString("uuid"), rs.getLong("total_points")));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка обновления топа по поинтам", e);
            return;
        }
        topPointsCache = result;
    }

    public void closeConnection() {
        for (PlayerBooster booster : cache.values()) save(booster);
        cache.clear();

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка закрытия соединения с базой данных", e);
        }
    }
}
