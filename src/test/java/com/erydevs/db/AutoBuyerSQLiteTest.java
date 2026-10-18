package com.erydevs.db;

import com.erydevs.EryBuyer;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.util.*;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AutoBuyerSQLiteTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void settingsSurviveReconnectAndKeepPlayersSeparate() throws Exception {
        EryBuyer plugin = mock(EryBuyer.class);
        java.lang.reflect.Field dataFolder = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredField("dataFolder");
        dataFolder.setAccessible(true);
        dataFolder.set(plugin, folder.getRoot());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        SQLite database = new SQLite(plugin);
        try {
            assertTrue(database.loadAutoBuyerSettings(first).isEmpty());
            database.saveAutoBuyerSetting(first, "DIAMOND", false);
            database.saveAutoBuyerSetting(first, "EMERALD", true);
            database.saveAutoBuyerSetting(second, "DIAMOND", true);
            database.saveAutoBuyerSetting(first, "EMERALD", false);
        } finally {
            database.closeConnection();
        }
        database = new SQLite(plugin);
        try {
            Map<String, Boolean> saved = database.loadAutoBuyerSettings(first);
            assertEquals(2, saved.size());
            assertEquals(Boolean.FALSE, saved.get("DIAMOND"));
            assertEquals(Boolean.FALSE, saved.get("EMERALD"));
            assertEquals(Boolean.TRUE, database.loadAutoBuyerSettings(second).get("DIAMOND"));
        } finally {
            database.closeConnection();
        }
    }
}
