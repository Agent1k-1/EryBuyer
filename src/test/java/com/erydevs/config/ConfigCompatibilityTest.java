package com.erydevs.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import java.io.File;
import java.lang.reflect.Field;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigCompatibilityTest {
    @Test
    public void newConfigDoesNotGetAutobuyerSettingsAdded() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        when(plugin.getConfig()).thenReturn(config);
        new Configs(plugin).loadConfigs();
        assertFalse(config.contains("autobuyer-settings", true));
        verify(plugin, never()).saveConfig();
    }

    @Test
    public void legacyAutobuyerBlockIsRemoved() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        config.set("autobuyer-settings.default-item-enabled", false);
        config.set("autobuyer-settings.item-status.enabled", "old");
        when(plugin.getConfig()).thenReturn(config);
        new Configs(plugin).loadConfigs();
        assertFalse(config.contains("autobuyer-settings", true));
        assertTrue(config.getStringList("register-menu").contains("menu/food.yml"));
        verify(plugin).saveConfig();
    }

    @Test
    public void itemStatusTextsAreReadFromMessages() throws Exception {
        MessagesConfig messages = messages(new YamlConfiguration());
        YamlConfiguration config = (YamlConfiguration) messages.getMessages();
        config.set("placeholder.enable-item-autobuyer", "&6Да");
        config.set("placeholder.disable-item-autobuyer", "&4Нет");
        assertEquals("&6Да", messages.getPlaceholderEnableItemAutobuyer());
        assertEquals("&4Нет", messages.getPlaceholderDisableItemAutobuyer());
    }

    @Test
    public void olderMessagesKeepDefaultItemStatuses() throws Exception {
        MessagesConfig messages = messages(new YamlConfiguration());
        assertEquals("&a✔ [Включено]", messages.getPlaceholderEnableItemAutobuyer());
        assertEquals("&c✘ [Выключено]", messages.getPlaceholderDisableItemAutobuyer());
    }

    @Test
    public void olderConfigGetsUpdateCheckSwitch() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        config.set("check-updates", null);
        when(plugin.getConfig()).thenReturn(config);
        new Configs(plugin).loadConfigs();
        assertTrue(config.getBoolean("check-updates"));
        verify(plugin).saveConfig();
    }

    private MessagesConfig messages(YamlConfiguration config) throws Exception {
        MessagesConfig result = new MessagesConfig(mock(JavaPlugin.class));
        Field field = MessagesConfig.class.getDeclaredField("messages");
        field.setAccessible(true);
        field.set(result, config);
        return result;
    }
}
