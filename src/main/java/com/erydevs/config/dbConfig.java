package com.erydevs.config;

import com.erydevs.EryBuyer;
import com.erydevs.db.*;
import com.erydevs.db.type.DatabaseType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class dbConfig {

    private final Database database;
    private final DatabaseType type;
    private final String fileName;

    public dbConfig(@NotNull EryBuyer plugin) {
        File file = new File(plugin.getDataFolder(), "db.yml");
        if (!file.exists()) plugin.saveResource("db.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.type = DatabaseType.fromName(config.getString("database-type.type"));
        if (this.type == DatabaseType.YAML) {
            this.fileName = config.getString("yaml.file");
            this.database = new YamlDatabase(plugin, fileName);
        } else if (this.type == DatabaseType.MYSQL) {
            ConfigurationSection section = config.getConfigurationSection("mysql");
            if (section == null) section = config.createSection("mysql");
            this.fileName = section.getString("database");
            this.database = new MySQL(plugin, section);
        } else {
            this.fileName = config.getString("sqlite.file");
            this.database = new SQLite(plugin);
        }
    }

    @NotNull
    public Database getDatabase() {
        return database;
    }

    @NotNull
    public DatabaseType getType() {
        return type;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

    public void close() {
        database.closeConnection();
    }
}
