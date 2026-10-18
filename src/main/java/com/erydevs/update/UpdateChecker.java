package com.erydevs.update;

import com.erydevs.EryBuyer;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker {
    private static final String REPOSITORY = "Agent1k-1/EryBuyer";
    private static final String RELEASE_URL = "https://github.com/" + REPOSITORY + "/releases/latest";
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "^v?(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-up)?$",
            Pattern.CASE_INSENSITIVE);

    private final EryBuyer plugin;
    private final ReleaseFetcher fetcher;
    private final AtomicBoolean checked = new AtomicBoolean();

    public UpdateChecker(EryBuyer plugin) {
        this(plugin, UpdateChecker::fetchLatestRelease);
    }

    UpdateChecker(EryBuyer plugin, ReleaseFetcher fetcher) {
        this.plugin = plugin;
        this.fetcher = fetcher;
    }

    public void check() {
        if (!plugin.getConfig().getBoolean("check-updates")) return;
        if (!checked.compareAndSet(false, true)) return;
        String currentVersion = plugin.getDescription().getVersion();
        plugin.getScheduler().runAsync(() -> checkRelease(currentVersion));
    }

    private void checkRelease(String currentVersion) {
        if (!plugin.isEnabled()) return;
        try {
            HttpResponse<String> response = fetcher.fetch();
            if (!plugin.isEnabled()) return;
            if (response.statusCode() != 200) {
                plugin.getLogger().fine("Проверка обновлений: GitHub вернул HTTP " + response.statusCode());
                return;
            }
            Release release = new Gson().fromJson(response.body(), Release.class);
            if (release == null || release.tag_name == null || release.draft || release.prerelease) return;
            String latestVersion = release.tag_name.trim();
            if (compareVersions(latestVersion, currentVersion) <= 0) {
                plugin.getLogger().info("Вы используете актуальную версию");
                return;
            }
            plugin.getLogger().info("Доступно обновление EryBuyer: " + currentVersion + " → " + latestVersion);
            plugin.getLogger().info("Скачать: " + RELEASE_URL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().fine("Не удалось проверить обновления: " + e.getMessage());
        }
    }

    private static HttpResponse<String> fetchLatestRelease() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + REPOSITORY + "/releases/latest"))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2026-03-10")
                .header("User-Agent", "EryBuyer-UpdateChecker")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static int compareVersions(String first, String second) {
        BigInteger[] left = parseVersion(first);
        BigInteger[] right = parseVersion(second);
        for (int i = 0; i < left.length; i++) {
            int comparison = left[i].compareTo(right[i]);
            if (comparison != 0) return comparison;
        }
        return 0;
    }

    private static BigInteger[] parseVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("Неподдерживаемая версия: " + version);
        BigInteger[] parts = new BigInteger[3];
        for (int i = 0; i < parts.length; i++) {
            String value = matcher.group(i + 1);
            parts[i] = value == null ? BigInteger.ZERO : new BigInteger(value);
        }
        return parts;
    }

    interface ReleaseFetcher {
        HttpResponse<String> fetch() throws IOException, InterruptedException;
    }

    private static final class Release {
        private String tag_name;
        private boolean draft;
        private boolean prerelease;
    }
}
