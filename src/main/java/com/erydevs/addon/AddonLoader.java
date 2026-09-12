package com.erydevs.addon;

import com.erydevs.EryBuyer;
import com.erydevs.addon.exception.AddonLoadException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonLoader {

    private final EryBuyer plugin;

    public AddonLoader(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public List<Addon> loadAll(@NotNull File addonsFolder) {
        List<Addon> loaded = new ArrayList<>();

        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
            return loaded;
        }

        File[] files = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) return loaded;

        for (File file : files) {
            try {
                loaded.add(loadAddon(file));
            } catch (AddonLoadException e) {
                plugin.getLogger().warning("Не удалось загрузить аддон " + file.getName() + ": " + e.getMessage());
            }
        }

        return loaded;
    }

    @NotNull
    private Addon loadAddon(@NotNull File file) throws AddonLoadException {
        AddonDescription description = readDescription(file);

        JarLoader classLoader;
        try {
            classLoader = new JarLoader(new URL[]{file.toURI().toURL()}, plugin.getClass().getClassLoader());
        } catch (MalformedURLException e) {
            throw new AddonLoadException("Некорректный путь к jar: " + file.getName(), e);
        }

        Class<?> mainClass;
        try {
            mainClass = Class.forName(description.getMain(), true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new AddonLoadException("Класс " + description.getMain() + " не найден в " + file.getName(), e);
        }

        if (!Addon.class.isAssignableFrom(mainClass)) {
            throw new AddonLoadException("Класс " + description.getMain() + " не реализует Addon (" + file.getName() + ")");
        }

        try {
            return (Addon) mainClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AddonLoadException("Не удалось создать экземпляр " + description.getMain(), e);
        }
    }

    @NotNull
    private AddonDescription readDescription(@NotNull File file) throws AddonLoadException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("addon.yml");
            if (entry == null) {
                throw new AddonLoadException("addon.yml не найден в " + file.getName());
            }

            try (InputStream in = jar.getInputStream(entry)) {
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));

                String main = cfg.getString("main");
                String name = cfg.getString("name");
                String version = cfg.getString("version");

                if (main == null || name == null) {
                    throw new AddonLoadException("addon.yml должен содержать main и name (" + file.getName() + ")");
                }

                return new AddonDescription(name, main, version);
            }
        } catch (IOException e) {
            throw new AddonLoadException("Ошибка чтения " + file.getName(), e);
        }
    }
}
