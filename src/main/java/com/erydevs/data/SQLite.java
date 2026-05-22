package com.erydevs.data;

import com.erydevs.data.connect.SQLiteConnect;
import com.erydevs.levels.PlayerLevel;

import java.sql.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SQLite {

    private final SQLiteConnect databaseConnect;
    private final Map<UUID, PlayerLevel> cache = new ConcurrentHashMap<>();
    private volatile List<Map.Entry<String, Double>> topPlayersCache = new ArrayList<>();

    public SQLite(File dataFolder, String fileName) {
        this.databaseConnect = new SQLiteConnect(dataFolder, fileName);
        if (databaseConnect.isConnected()) {
            createTable();
        }
    }

    private void createTable() {
        try (Statement stmt = databaseConnect.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_levels (" +
                    "uuid TEXT PRIMARY KEY," +
                    "current_level INTEGER DEFAULT 1," +
                    "total_earned REAL DEFAULT 0.0" +
                    ")");
        } catch (SQLException e) {
        }
    }

    public PlayerLevel getPlayerData(UUID uuid) {
        PlayerLevel cached = cache.get(uuid);
        if (cached != null) return cached;

        if (!databaseConnect.isConnected()) {
            PlayerLevel def = new PlayerLevel(uuid, 1, 0.0);
            cache.put(uuid, def);
            return def;
        }

        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "SELECT current_level, total_earned FROM player_levels WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerLevel pl = new PlayerLevel(uuid, rs.getInt("current_level"), rs.getDouble("total_earned"));
                cache.put(uuid, pl);
                return pl;
            }
        } catch (SQLException e) {
        }
        PlayerLevel def = new PlayerLevel(uuid, 1, 0.0);
        cache.put(uuid, def);
        return def;
    }

    public void savePlayerData(PlayerLevel player) {
        cache.put(player.getUuid(), player);
        if (!databaseConnect.isConnected()) return;

        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_levels (uuid, current_level, total_earned) VALUES (?, ?, ?)")) {
            stmt.setString(1, player.getUuid().toString());
            stmt.setInt(2, player.getCurrentLevel());
            stmt.setDouble(3, player.getTotalEarned());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения данных игрока: " + e.getMessage());
        }
    }

    public void flushPlayerAsync(PlayerLevel player) {
        cache.put(player.getUuid(), player);
        if (!databaseConnect.isConnected()) return;

        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_levels (uuid, current_level, total_earned) VALUES (?, ?, ?)")) {
            stmt.setString(1, player.getUuid().toString());
            stmt.setInt(2, player.getCurrentLevel());
            stmt.setDouble(3, player.getTotalEarned());
            stmt.executeUpdate();
        } catch (SQLException e) {
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
            flushPlayerAsync(player);
        }
    }

    public List<Map.Entry<String, Double>> getTopPlayers(int limit, double minEarned) {
        return topPlayersCache;
    }

    public void refreshTopPlayersCache(double minEarned) {
        if (!databaseConnect.isConnected()) return;

        List<Map.Entry<String, Double>> result = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "SELECT uuid, total_earned FROM player_levels WHERE total_earned >= ? ORDER BY total_earned DESC LIMIT 100")) {
            stmt.setDouble(1, minEarned);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new AbstractMap.SimpleEntry<>(rs.getString("uuid"), rs.getDouble("total_earned")));
            }
        } catch (SQLException e) {
        }
        topPlayersCache = result;
    }

    public void updateTopPlayers(double minEarned) {
        refreshTopPlayersCache(minEarned);
    }

    public void closeConnection() {
        for (PlayerLevel player : cache.values()) {
            flushPlayerAsync(player);
        }
        cache.clear();
        databaseConnect.closeConnection();
    }

    public boolean isConnected() {
        return databaseConnect.isConnected();
    }
}
