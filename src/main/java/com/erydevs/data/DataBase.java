package com.erydevs.data;

import com.erydevs.levels.PlayerLevel;
import java.sql.*;
import java.io.File;
import java.util.UUID;

public class DataBase {

    private Connection connection;
    private final File dbFile;
    private static final int MAX_RETRIES = 3;

    public DataBase(File dataFolder) {
        this.dbFile = new File(dataFolder, "playerdata.db");
        connect();
        if (connection != null) {
            createTable();
        } else {
            System.err.println("Не удалось подключиться к базе данных!");
        }
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            System.out.println("SQLite база данных инициализирована: " + dbFile.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка: SQLite JDBC драйвер не найден!");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
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
            System.err.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }

    public PlayerLevel getPlayerData(UUID uuid) {
        if (connection == null) {
            System.err.println("Ошибка: соединение с БД не инициализировано");
            return new PlayerLevel(uuid, 1, 0.0);
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT current_level, total_earned FROM player_levels WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerLevel(uuid, rs.getInt("current_level"), rs.getDouble("total_earned"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка чтения данных игрока: " + e.getMessage());
        }
        return new PlayerLevel(uuid, 1, 0.0);
    }

    public void savePlayerData(PlayerLevel player) {
        if (connection == null) {
            System.err.println("Ошибка: соединение с БД не инициализировано");
            return;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(
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
        if (connection == null) {
            System.err.println("Ошибка: соединение с БД не инициализировано");
            return;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE player_levels SET total_earned = total_earned + ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, uuid.toString());
            int updated = stmt.executeUpdate();
            
            if (updated == 0) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO player_levels (uuid, current_level, total_earned) VALUES (?, 1, ?)")) {
                    insert.setString(1, uuid.toString());
                    insert.setDouble(2, amount);
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка операции с БД: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия БД: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        if (connection == null) return false;
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
