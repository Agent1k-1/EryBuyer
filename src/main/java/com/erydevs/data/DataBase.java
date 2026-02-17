package com.erydevs.data;

import com.erydevs.levels.PlayerLevel;
import java.sql.*;
import java.io.File;
import java.util.UUID;

public class DataBase {

    private Connection connection;
    private final File dbFile;

    public DataBase(File dataFolder) {
        this.dbFile = new File(dataFolder, "playerdata.db");
        connect();
        createTable();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_levels (" +
                    "uuid TEXT PRIMARY KEY," +
                    "current_level INTEGER DEFAULT 1," +
                    "total_earned REAL DEFAULT 0.0" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerLevel getPlayerData(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT current_level, total_earned FROM player_levels WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerLevel(uuid, rs.getInt("current_level"), rs.getDouble("total_earned"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerLevel(uuid, 1, 0.0);
    }

    public void savePlayerData(PlayerLevel player) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_levels (uuid, current_level, total_earned) VALUES (?, ?, ?)")) {
            stmt.setString(1, player.getUuid().toString());
            stmt.setInt(2, player.getCurrentLevel());
            stmt.setDouble(3, player.getTotalEarned());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayerEarnings(UUID uuid, double amount) {
        PlayerLevel player = getPlayerData(uuid);
        player.addEarnings(amount);
        savePlayerData(player);
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
