package com.erydevs.data;

import com.erydevs.levels.PlayerLevel;
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

    private static final String TABLE_PLAYERS = "buyer_players";

    private final Logger logger;
    private final File dbFile;
    private final Map<UUID, PlayerLevel> cache = new ConcurrentHashMap<>();
    private volatile List<Map.Entry<String, Double>> topPlayersCache = new ArrayList<>();

    private Connection connection;

    public SQLite(File dataFolder, String fileName, Logger logger) {
        this.logger = logger;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dbFile = new File(dataFolder, fileName);
        connect();
        if (isConnected()) {
            createTable();
        }
    }

    private void connect() {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.DELETE);
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
            config.enforceForeignKeys(true);

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath(), config.toProperties());
            logger.info("База данных SQLite успешно подключена: " + dbFile.getName());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка подключения к базе данных", e);
        }
    }

    private synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
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
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAYERS + " (" +
                "uuid TEXT PRIMARY KEY NOT NULL, " +
                "current_level INTEGER NOT NULL DEFAULT 1, " +
                "total_earned REAL NOT NULL DEFAULT 0.0" +
                ")";
        String index = "CREATE INDEX IF NOT EXISTS idx_buyer_players_total_earned " +
                "ON " + TABLE_PLAYERS + " (total_earned DESC)";

        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
            statement.executeUpdate(index);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка создания таблицы базы данных", e);
        }
    }

    public PlayerLevel getPlayerData(UUID uuid) {
        PlayerLevel cached = cache.get(uuid);
        if (cached != null) return cached;

        if (!isConnected()) {
            return cacheDefault(uuid);
        }

        String sql = "SELECT current_level, total_earned FROM " + TABLE_PLAYERS + " WHERE uuid = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    PlayerLevel playerLevel = new PlayerLevel(uuid, result.getInt("current_level"), result.getDouble("total_earned"));
                    cache.put(uuid, playerLevel);
                    return playerLevel;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка загрузки данных игрока " + uuid, e);
        }

        return cacheDefault(uuid);
    }

    private PlayerLevel cacheDefault(UUID uuid) {
        PlayerLevel playerLevel = new PlayerLevel(uuid, 1, 0.0);
        cache.put(uuid, playerLevel);
        return playerLevel;
    }

    public void savePlayerData(PlayerLevel player) {
        cache.put(player.getUuid(), player);
        persist(player, true);
    }

    public void flushPlayerAsync(PlayerLevel player) {
        cache.put(player.getUuid(), player);
        persist(player, false);
    }

    private void persist(PlayerLevel player, boolean logErrors) {
        if (!isConnected()) return;

        String sql = "INSERT INTO " + TABLE_PLAYERS + " (uuid, current_level, total_earned) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET current_level = excluded.current_level, total_earned = excluded.total_earned";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, player.getUuid().toString());
            statement.setInt(2, player.getCurrentLevel());
            statement.setDouble(3, player.getTotalEarned());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (logErrors) {
                logger.log(Level.WARNING, "Ошибка сохранения данных игрока " + player.getUuid(), e);
            }
        }
    }

    public void addPlayerEarnings(UUID uuid, double amount) {
        PlayerLevel cached = cache.get(uuid);
        if (cached != null) {
            cached.addEarnings(amount);
        }
    }

    public void evictPlayer(UUID uuid) {
        PlayerLevel player = cache.remove(uuid);
        if (player != null) {
            persist(player, true);
        }
    }

    public List<Map.Entry<String, Double>> getTopPlayers(int limit, double minEarned) {
        return topPlayersCache;
    }

    public void refreshTopPlayersCache(double minEarned) {
        if (!isConnected()) return;

        String sql = "SELECT uuid, total_earned FROM " + TABLE_PLAYERS +
                " WHERE total_earned >= ? ORDER BY total_earned DESC LIMIT 100";

        List<Map.Entry<String, Double>> result = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setDouble(1, minEarned);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new AbstractMap.SimpleEntry<>(rs.getString("uuid"), rs.getDouble("total_earned")));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка обновления таблицы лидеров", e);
            return;
        }

        topPlayersCache = result;
    }

    public void updateTopPlayers(double minEarned) {
        refreshTopPlayersCache(minEarned);
    }

    public void closeConnection() {
        for (PlayerLevel player : cache.values()) {
            persist(player, true);
        }
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