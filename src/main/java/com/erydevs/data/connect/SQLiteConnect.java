package com.erydevs.data.connect;

import java.io.File;
import java.sql.*;

public class SQLiteConnect {

    private Connection connection;
    private final File dbFile;

    public SQLiteConnect(File dataFolder, String fileName) {
        this.dbFile = new File(dataFolder, fileName);
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            System.out.println("База данных подключена");
        } catch (ClassNotFoundException e) {
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        if (connection == null) return false;
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия БД: " + e.getMessage());
        }
    }
}