package com.erydevs.db;

public enum DatabaseType {
    SQLITE("sqlite"),
    MYSQL("mysql"),
    YAML("yaml");

    private final String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public static DatabaseType fromName(String name) {
        if (name == null) return SQLITE;
        String value = name.trim().toLowerCase();
        if (value.equals(YAML.name)) return YAML;
        if (value.equals(MYSQL.name)) return MYSQL;
        return SQLITE;
    }

    public String getName() {
        return name;
    }
}
