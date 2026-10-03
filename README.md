# EryBuyer API

[Русский](#русский) | [English](#english)

---

## Русский

API EryBuyer позволяет другим плагинам читать данные игроков (уровень бустера, очки, множители, автопродажа, топ) и реагировать на события продажи и повышения уровня. Также можно писать аддоны, которые EryBuyer загружает сам из папки `plugins/EryBuyer/addons/`.

### 1. Подключение

В `plugin.yml` вашего плагина укажите зависимость, чтобы EryBuyer загрузился раньше:

```yaml
depend: [EryBuyer]
# или, если API необязательно:
softdepend: [EryBuyer]
```

Добавьте EryBuyer в зависимости проекта со scope `provided`.

Через JitPack:

```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.ВАШ_АККАУНТ</groupId>
    <artifactId>EryBuyer</artifactId>
    <version>ТЕГ_РЕЛИЗА</version>
    <scope>provided</scope>
</dependency>
```

### 2. Получение API

API регистрируется в `onEnable()` EryBuyer и снимается при его выключении.

```java
import com.erydevs.api.BuyerAPI;
import com.erydevs.api.EryBuyerAPI;

BuyerAPI api = EryBuyerAPI.getInstance();
```

- `EryBuyerAPI.getInstance()` — бросает `IllegalStateException`, если API ещё не зарегистрировано.
- `EryBuyerAPI.getInstanceOrNull()` — возвращает `null`, если EryBuyer не загружен. Используйте, когда EryBuyer указан в `softdepend`.

Вызывайте API после включения EryBuyer (в `onEnable()` вашего плагина при `depend`, не в статических полях и не в конструкторе).

### 3. Методы `BuyerAPI`

| Метод | Описание |
|---|---|
| `int getBoosterLevel(UUID)` | Текущий уровень бустера игрока |
| `long getPoints(UUID)` | Всего очков игрока |
| `void addPoints(Player, long)` | Начислить очки; при достижении порога повышает уровень и вызывает `PlayerLevelUpEvent`. Данные сохраняются асинхронно |
| `double getMoneyMultiplier(UUID)` | Множитель денег игрока |
| `double getBoosterMultiplier(UUID)` | Множитель бустера игрока |
| `boolean isAutobuyerEnabled(Player)` | Включена ли автопродажа |
| `void toggleAutobuyer(Player)` | Переключить автопродажу |
| `List<Map.Entry<String, Long>> getTopPoints()` | Топ игроков по очкам (ник → очки) |
| `void registerAddon(Addon)` | Зарегистрировать аддон |
| `void unregisterAddon(String)` | Снять регистрацию аддона по имени |
| `Collection<Addon> getAddons()` | Зарегистрированные аддоны |

Пример:

```java
BuyerAPI api = EryBuyerAPI.getInstance();
UUID uuid = player.getUniqueId();

int level = api.getBoosterLevel(uuid);
long points = api.getPoints(uuid);
double multiplier = api.getMoneyMultiplier(uuid);

api.addPoints(player, 100);

if (!api.isAutobuyerEnabled(player)) {
    api.toggleAutobuyer(player);
}
```

`addPoints` и `toggleAutobuyer` вызывайте из основного потока сервера.

### 4. События

Оба события не отменяемые и вызываются в основном потоке.

**`PlayerSellEvent`** — игрок продал предметы (вручную через меню или автопродажей).

| Метод | Описание |
|---|---|
| `getPlayer()` | Игрок |
| `getMaterial()` | Материал предмета |
| `getAmount()` | Количество проданного |
| `getPrice()` | Итоговая цена |
| `getPointsEarned()` | Полученные очки |

**`PlayerLevelUpEvent`** — игрок повысил уровень бустера.

| Метод | Описание |
|---|---|
| `getPlayer()` | Игрок |
| `getOldLevel()` | Прошлый уровень |
| `getNewLevel()` | Новый уровень |

```java
import com.erydevs.api.event.PlayerLevelUpEvent;
import com.erydevs.api.event.PlayerSellEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BuyerListener implements Listener {

    @EventHandler
    public void onSell(PlayerSellEvent event) {
        event.getPlayer().sendMessage("Продано " + event.getAmount() + " " + event.getMaterial()
                + " за " + event.getPrice() + ", очков: " + event.getPointsEarned());
    }

    @EventHandler
    public void onLevelUp(PlayerLevelUpEvent event) {
        event.getPlayer().sendMessage("Уровень " + event.getOldLevel() + " → " + event.getNewLevel());
    }
}
```

Зарегистрируйте слушатель как обычно: `getServer().getPluginManager().registerEvents(new BuyerListener(), this);`

### 5. Аддоны

Аддон — отдельный jar, который EryBuyer загружает при старте из `plugins/EryBuyer/addons/`. Для сборки нужен полный jar EryBuyer (интерфейс `com.erydevs.addon.Addon`).

**1) Главный класс:**

```java
import com.erydevs.EryBuyer;
import com.erydevs.addon.Addon;
import org.jetbrains.annotations.NotNull;

public class MyAddon implements Addon {

    @Override
    public @NotNull String getName() {
        return "MyAddon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public void onEnable(@NotNull EryBuyer plugin) {
        plugin.getLogger().info("MyAddon включён");
    }

    @Override
    public void onDisable() {
    }
}
```

У класса должен быть публичный конструктор без аргументов.

**2) Файл `addon.yml`** в корне jar аддона:

```yaml
name: MyAddon
main: com.example.myaddon.MyAddon
version: 1.0
```

`name` и `main` обязательны.

**3)** Положите jar в `plugins/EryBuyer/addons/` и перезапустите сервер. При успехе в консоли появится `Аддон загружен: MyAddon v1.0`.

Аддон загружается через отдельный загрузчик классов с родителем из EryBuyer, поэтому ему доступны классы EryBuyer, Bukkit и зависимости EryBuyer (например Vault). Слушатели событий аддон регистрирует через `plugin` из `onEnable`.

---

## English

The EryBuyer API lets other plugins read player data (booster level, points, multipliers, autobuyer state, leaderboard) and react to sell and level-up events. You can also write addons that EryBuyer loads itself from the `plugins/EryBuyer/addons/` folder.

### 1. Setup

Declare EryBuyer in your plugin's `plugin.yml` so it loads first:

```yaml
depend: [EryBuyer]
# or, if the API is optional:
softdepend: [EryBuyer]
```

Add EryBuyer to your project dependencies with the `provided` scope.

Via JitPack:

```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.YOUR_ACCOUNT</groupId>
    <artifactId>EryBuyer</artifactId>
    <version>RELEASE_TAG</version>
    <scope>provided</scope>
</dependency>
```

### 2. Getting the API

The API is registered in EryBuyer's `onEnable()` and unregistered when it shuts down.

```java
import com.erydevs.api.BuyerAPI;
import com.erydevs.api.EryBuyerAPI;

BuyerAPI api = EryBuyerAPI.getInstance();
```

- `EryBuyerAPI.getInstance()` throws `IllegalStateException` if the API is not registered yet.
- `EryBuyerAPI.getInstanceOrNull()` returns `null` if EryBuyer is not loaded. Use it when EryBuyer is a `softdepend`.

Call the API after EryBuyer has enabled (in your plugin's `onEnable()` when using `depend`; not in static fields or constructors).

### 3. `BuyerAPI` methods

| Method | Description |
|---|---|
| `int getBoosterLevel(UUID)` | Player's current booster level |
| `long getPoints(UUID)` | Player's total points |
| `void addPoints(Player, long)` | Add points; levels the player up when a threshold is reached and fires `PlayerLevelUpEvent`. Data is saved asynchronously |
| `double getMoneyMultiplier(UUID)` | Player's money multiplier |
| `double getBoosterMultiplier(UUID)` | Player's booster multiplier |
| `boolean isAutobuyerEnabled(Player)` | Whether the autobuyer is on |
| `void toggleAutobuyer(Player)` | Toggle the autobuyer |
| `List<Map.Entry<String, Long>> getTopPoints()` | Leaderboard by points (name → points) |
| `void registerAddon(Addon)` | Register an addon |
| `void unregisterAddon(String)` | Unregister an addon by name |
| `Collection<Addon> getAddons()` | Registered addons |

Example:

```java
BuyerAPI api = EryBuyerAPI.getInstance();
UUID uuid = player.getUniqueId();

int level = api.getBoosterLevel(uuid);
long points = api.getPoints(uuid);
double multiplier = api.getMoneyMultiplier(uuid);

api.addPoints(player, 100);

if (!api.isAutobuyerEnabled(player)) {
    api.toggleAutobuyer(player);
}
```

Call `addPoints` and `toggleAutobuyer` from the main server thread.

### 4. Events

Both events are not cancellable and are fired on the main thread.

**`PlayerSellEvent`** — a player sold items (manually through the menu or via the autobuyer).

| Method | Description |
|---|---|
| `getPlayer()` | The player |
| `getMaterial()` | Item material |
| `getAmount()` | Amount sold |
| `getPrice()` | Total price |
| `getPointsEarned()` | Points earned |

**`PlayerLevelUpEvent`** — a player's booster level went up.

| Method | Description |
|---|---|
| `getPlayer()` | The player |
| `getOldLevel()` | Previous level |
| `getNewLevel()` | New level |

```java
import com.erydevs.api.event.PlayerLevelUpEvent;
import com.erydevs.api.event.PlayerSellEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BuyerListener implements Listener {

    @EventHandler
    public void onSell(PlayerSellEvent event) {
        event.getPlayer().sendMessage("Sold " + event.getAmount() + " " + event.getMaterial()
                + " for " + event.getPrice() + ", points: " + event.getPointsEarned());
    }

    @EventHandler
    public void onLevelUp(PlayerLevelUpEvent event) {
        event.getPlayer().sendMessage("Level " + event.getOldLevel() + " → " + event.getNewLevel());
    }
}
```

Register the listener as usual: `getServer().getPluginManager().registerEvents(new BuyerListener(), this);`

### 5. Addons

An addon is a separate jar that EryBuyer loads at startup from `plugins/EryBuyer/addons/`. Building one requires the full EryBuyer jar (the `com.erydevs.addon.Addon` interface).

**1) Main class:**

```java
import com.erydevs.EryBuyer;
import com.erydevs.addon.Addon;
import org.jetbrains.annotations.NotNull;

public class MyAddon implements Addon {

    @Override
    public @NotNull String getName() {
        return "MyAddon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public void onEnable(@NotNull EryBuyer plugin) {
        plugin.getLogger().info("MyAddon enabled");
    }

    @Override
    public void onDisable() {
    }
}
```

The class must have a public no-argument constructor.

**2) `addon.yml`** at the root of the addon jar:

```yaml
name: MyAddon
main: com.example.myaddon.MyAddon
version: 1.0
```

`name` and `main` are required.

**3)** Put the jar into `plugins/EryBuyer/addons/` and restart the server. On success the console prints `Аддон загружен: MyAddon v1.0`.

The addon is loaded through its own class loader whose parent is EryBuyer's, so it can use EryBuyer, Bukkit and EryBuyer's dependency classes (for example Vault). Register event listeners from the addon using the `plugin` instance passed to `onEnable`.
