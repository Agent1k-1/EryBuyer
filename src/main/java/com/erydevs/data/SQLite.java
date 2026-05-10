package com.erydevs.data;

import com.erydevs.data.connect.SQLiteConnect;
import com.erydevs.levels.PlayerLevel;

import java.sql.*;
import java.io.File;
import java.util.*;

public class SQLite {

    private final SQLiteConnect databaseConnect;

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
        if (!databaseConnect.isConnected()) {
            return new PlayerLevel(uuid, 1, 0.0);
        }

        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "SELECT current_level, total_earned FROM player_levels WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerLevel(uuid, rs.getInt("current_level"), rs.getDouble("total_earned"));
            }
        } catch (SQLException e) {
        }
        return new PlayerLevel(uuid, 1, 0.0);
    }

    public void savePlayerData(PlayerLevel player) {
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

    public void addPlayerEarnings(UUID uuid, double amount) {
        if (!databaseConnect.isConnected()) return;

        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "UPDATE player_levels SET total_earned = total_earned + ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, uuid.toString());
            int updated = stmt.executeUpdate();

            if (updated == 0) {
                try (PreparedStatement insert = databaseConnect.getConnection().prepareStatement(
                        "INSERT INTO player_levels (uuid, current_level, total_earned) VALUES (?, 1, ?)")) {
                    insert.setString(1, uuid.toString());
                    insert.setDouble(2, amount);
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
        }
    }

    public List<Map.Entry<String, Double>> getTopPlayers(int limit, double minEarned) {
        if (!databaseConnect.isConnected()) return new ArrayList<>();

        List<Map.Entry<String, Double>> topPlayers = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnect.getConnection().prepareStatement(
                "SELECT uuid, total_earned FROM player_levels WHERE total_earned >= ? ORDER BY total_earned DESC LIMIT ?")) {
            stmt.setDouble(1, minEarned);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topPlayers.add(new AbstractMap.SimpleEntry<>(rs.getString("uuid"), rs.getDouble("total_earned")));
            }
        } catch (SQLException e) {
        }
        return topPlayers;
    }

    public void updateTopPlayers(double minEarned) {
        if (!databaseConnect.isConnected()) return;
        try {
            getTopPlayers(Integer.MAX_VALUE, minEarned);
        } catch (Exception e) {
        }
    }

    public void closeConnection() {
        databaseConnect.closeConnection();
    }

    public boolean isConnected() {
        return databaseConnect.isConnected();
    }
}