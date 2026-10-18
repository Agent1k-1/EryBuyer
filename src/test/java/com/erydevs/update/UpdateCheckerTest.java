package com.erydevs.update;

import com.erydevs.EryBuyer;
import com.erydevs.folia.Scheduler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UpdateCheckerTest {
    private EryBuyer plugin;
    private Scheduler scheduler;
    private Logger logger;
    private YamlConfiguration config;

    @Before
    public void setup() throws Exception {
        plugin = mock(EryBuyer.class);
        scheduler = mock(Scheduler.class);
        logger = mock(Logger.class);
        config = new YamlConfiguration();
        config.set("check-updates", true);
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getScheduler()).thenReturn(scheduler);
        when(plugin.getLogger()).thenReturn(logger);
        setPluginField("description", new PluginDescriptionFile("EryBuyer", "v3", "com.erydevs.EryBuyer"));
        setPluginField("isEnabled", true);
        doAnswer(call -> {
            ((Runnable) call.getArgument(0)).run();
            return null;
        }).when(scheduler).runAsync(any());
    }

    @Test
    public void comparesNumericVersionsAndLegacyTags() {
        assertEquals(0, UpdateChecker.compareVersions("v3", "3.0.0"));
        assertEquals(0, UpdateChecker.compareVersions("v2-up", "2.0"));
        assertTrue(UpdateChecker.compareVersions("v2-up", "v3") < 0);
        assertTrue(UpdateChecker.compareVersions("v3.0.1", "v3") > 0);
        assertTrue(UpdateChecker.compareVersions("3.10.0", "3.9.9") > 0);
        assertTrue(UpdateChecker.compareVersions("10.0", "9.99") > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedVersionsAreNotGuessed() {
        UpdateChecker.compareVersions("release-latest", "v3");
    }

    @Test
    public void newerReleasePrintsVersionAndDownloadLink() {
        check(200, "{\"tag_name\":\"v3.0.1\"}");
        verify(logger).info("Доступно обновление EryBuyer: v3 → v3.0.1");
        verify(logger).info("Скачать: https://github.com/Agent1k-1/EryBuyer/releases/latest");
    }

    @Test
    public void olderReleaseDoesNotSuggestDowngrade() {
        check(200, "{\"tag_name\":\"v2-up\"}");
        verify(logger).info("Вы используете актуальную версию");
        verify(logger, times(1)).info(anyString());
    }

    @Test
    public void matchingReleasePrintsCurrentVersionMessage() {
        check(200, "{\"tag_name\":\"v3.0.0\"}");
        verify(logger).info("Вы используете актуальную версию");
        verify(logger, times(1)).info(anyString());
    }

    @Test
    public void configurationCanDisableAllNetworkWork() {
        config.set("check-updates", false);
        AtomicInteger requests = new AtomicInteger();
        new UpdateChecker(plugin, () -> {
            requests.incrementAndGet();
            return response(200, "{\"tag_name\":\"v4\"}");
        }).check();
        assertEquals(0, requests.get());
        verify(scheduler, never()).runAsync(any());
    }

    @Test
    public void requestRunsInSchedulerAndOnlyOnce() {
        AtomicReference<Runnable> task = new AtomicReference<>();
        doAnswer(call -> {
            task.set(call.getArgument(0));
            return null;
        }).when(scheduler).runAsync(any());
        AtomicInteger requests = new AtomicInteger();
        UpdateChecker checker = new UpdateChecker(plugin, () -> {
            requests.incrementAndGet();
            return response(200, "{\"tag_name\":\"v3\"}");
        });
        checker.check();
        checker.check();
        assertEquals(0, requests.get());
        assertNotNull(task.get());
        task.get().run();
        assertEquals(1, requests.get());
        verify(scheduler, times(1)).runAsync(any());
    }

    @Test
    public void githubErrorsDoNotFailPlugin() {
        for (int status : new int[]{403, 404, 429, 500}) {
            check(status, "{}");
        }
        new UpdateChecker(plugin, () -> {
            throw new IOException("offline");
        }).check();
        verify(logger, never()).info(anyString());
    }

    @Test
    public void invalidAndUnstableReleasesAreIgnored() {
        for (String body : new String[]{"not json", "null", "{}", "{\"tag_name\":\"release-latest\"}",
                "{\"tag_name\":\"v4\",\"prerelease\":true}", "{\"tag_name\":\"v4\",\"draft\":true}"}) {
            check(200, body);
        }
        verify(logger, never()).info(anyString());
    }

    @Test
    public void disabledPluginDoesNotLogLateResponse() {
        new UpdateChecker(plugin, () -> {
            try {
                setPluginField("isEnabled", false);
            } catch (Exception e) {
                throw new IOException(e);
            }
            return response(200, "{\"tag_name\":\"v4\"}");
        }).check();
        verifyNoInteractions(logger);
    }

    @Test
    public void interruptedRequestRestoresInterruptFlag() {
        try {
            new UpdateChecker(plugin, () -> {
                throw new InterruptedException();
            }).check();
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private void check(int status, String body) {
        new UpdateChecker(plugin, () -> response(status, body)).check();
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }

    private void setPluginField(String name, Object value) throws Exception {
        Field field = JavaPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }
}
