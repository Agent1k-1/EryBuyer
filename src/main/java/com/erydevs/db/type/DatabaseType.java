package com.erydevs.db.type;

public enum DatabaseType {
    SQLITE("sqlite"),
    MYSQL("mysql");

    private final String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public static DatabaseType fromName(String name) {
        if (name == null) return SQLITE;
        String value = name.trim().toLowerCase();
        if (value.equals(MYSQL.name)) return MYSQL;
        return SQLITE;
    }

    public String getName() {
        return name;
    }
}
