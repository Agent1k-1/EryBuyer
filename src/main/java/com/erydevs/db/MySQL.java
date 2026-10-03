package com.erydevs.db;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.mysql.cj.jdbc.Driver;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL implements Database {

    private final EryBuyer plugin;
    private final Logger logger;
    private final String url;
    private final Properties properties = new Properties();
    private final String table;
    private final String limitsTable;
    private final Map<UUID, PlayerBooster> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> limitCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tokensCache = new ConcurrentHashMap<>();
    private volatile List<Map.Entry<String, Long>> topPointsCache = new ArrayList<>();

    private Connection connection;

    public MySQL(@NotNull EryBuyer plugin, @NotNull ConfigurationSection section) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        String host = section.getString("host", "localhost");
        int port = section.getInt("port", 3306);
        String database = section.getString("database", "erybuyer");
        boolean useSsl = section.getBoolean("use-ssl", false);
        String prefix = section.getString("table-prefix", "");

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSsl
                + "&allowPublicKeyRetrieval=true"
                + "&useUnicode=true&characterEncoding=utf8"
                + "&serverTimezone=UTC";
        properties.setProperty("user", section.getString("username", "root"));
        properties.setProperty("password", section.getString("password", ""));

        this.table = prefix + "buyer_players";
        this.limitsTable = prefix + "buyer_limits";

        connect();
        if (isConnected()) {
            createTable();
            migrateTokensColumn();
            refreshTopPointsCache();
        }
    }

    private void connect() {
        try {
            connection = new Driver().connect(url, properties);
            logger.info("База данных MySQL подключена");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка подключения к базе данных MySQL", e);
        }
    }

    private synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) connect();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка проверки соединения с базой данных MySQL", e);
        }
        return connection;
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTable() {
        String create = "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "booster_level INT NOT NULL DEFAULT 0, " +
                "total_points BIGINT NOT NULL DEFAULT 0, " +
                "tokens INT NOT NULL DEFAULT 0) DEFAULT CHARSET=utf8mb4";

        String createLimits = "CREATE TABLE IF NOT EXISTS " + limitsTable + " (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "material VARCHAR(64) NOT NULL, " +
                "sold INT NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(uuid, material)) DEFAULT CHARSET=utf8mb4";

        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate(create);
            st.executeUpdate(createLimits);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка создания таблицы базы данных MySQL", e);
        }
    }

    private void migrateTokensColumn() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN tokens INT NOT NULL DEFAULT 0");
        } catch (SQLException ignored) {
        }
    }

    @Override
    public int getSoldAmount(@NotNull UUID uuid, @NotNull String material) {
        String key = uuid + ":" + material;
        Integer cached = limitCache.get(key);
        if (cached != null) return cached;
        if (!isConnected()) {
            limitCache.put(key, 0);
            return 0;
        }

        String sql = "SELECT sold FROM " + limitsTable + " WHERE uuid = ? AND material = ?";
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

    @Override
    public void addSoldAmount(@NotNull UUID uuid, @NotNull String material, int amount) {
        String key = uuid + ":" + material;
        int updated = getSoldAmount(uuid, material) + amount;
        limitCache.put(key, updated);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> writeSoldAmount(uuid, material, updated));
    }

    private void writeSoldAmount(@NotNull UUID uuid, @NotNull String material, int value) {
        if (!isConnected()) return;

        String sql = "INSERT INTO " + limitsTable + " (uuid, material, sold) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE sold = VALUES(sold)";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setString(2, material);
            st.setInt(3, value);
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сохранения лимита " + uuid + ":" + material, e);
        }
    }

    @Override
    public void resetAllLimits() {
        limitCache.clear();
        if (!isConnected()) return;

        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM " + limitsTable);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сброса лимитов", e);
        }
    }

    @Override
    public void resetLimitsForMaterial(@NotNull String material) {
        limitCache.entrySet().removeIf(e -> e.getKey().endsWith(":" + material));
        if (!isConnected()) return;

        String sql = "DELETE FROM " + limitsTable + " WHERE material = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, material);
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сброса лимита для " + material, e);
        }
    }

    @Override
    @NotNull
    public PlayerBooster getPlayerData(@NotNull UUID uuid) {
        PlayerBooster cached = cache.get(uuid);
        if (cached != null) return cached;
        if (!isConnected()) return cacheDefault(uuid);

        String sql = "SELECT booster_level, total_points FROM " + table + " WHERE uuid = ?";
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

    @Override
    public void save(@NotNull PlayerBooster booster) {
        cache.put(booster.getUuid(), booster);
        if (!isConnected()) return;

        String sql = "INSERT INTO " + table + " (uuid, booster_level, total_points) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE booster_level = VALUES(booster_level), total_points = VALUES(total_points)";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, booster.getUuid().toString());
            st.setInt(2, booster.getCurrentLevel());
            st.setLong(3, booster.getTotalPoints());
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сохранения данных игрока " + booster.getUuid(), e);
        }
    }

    @Override
    public void evictPlayer(@NotNull UUID uuid) {
        PlayerBooster booster = cache.remove(uuid);
        if (booster != null) save(booster);
    }

    @Override
    @NotNull
    public List<Map.Entry<String, Long>> getTopPoints() {
        return topPointsCache;
    }

    @Override
    public void refreshTopPointsCache() {
        if (!isConnected()) return;

        String sql = "SELECT uuid, total_points FROM " + table +
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

    @Override
    public int getTokens(@NotNull UUID uuid) {
        Integer cached = tokensCache.get(uuid);
        if (cached != null) return cached;
        if (!isConnected()) {
            tokensCache.put(uuid, 0);
            return 0;
        }

        String sql = "SELECT tokens FROM " + table + " WHERE uuid = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt("tokens");
                    tokensCache.put(uuid, value);
                    return value;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка загрузки жетонов " + uuid, e);
        }
        tokensCache.put(uuid, 0);
        return 0;
    }

    @Override
    public void addTokens(@NotNull UUID uuid, int amount) {
        int updated = Math.max(0, getTokens(uuid) + amount);
        tokensCache.put(uuid, updated);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> writeTokens(uuid, updated));
    }

    private void writeTokens(@NotNull UUID uuid, int value) {
        if (!isConnected()) return;

        String sql = "INSERT INTO " + table + " (uuid, tokens) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE tokens = VALUES(tokens)";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setInt(2, value);
            st.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка сохранения жетонов " + uuid, e);
        }
    }

    @Override
    public void closeConnection() {
        for (PlayerBooster booster : cache.values()) save(booster);
        cache.clear();

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Соединение с базой данных MySQL закрыто");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка закрытия соединения с базой данных MySQL", e);
        }
    }
}
