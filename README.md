# EryBuyer API

[Русский](#русский) | [English](#english)

---

## Русский

API EryBuyer позволяет другим плагинам читать данные игроков (уровень бустера, очки, множители, автопродажа, топ) и реагировать на события продажи и повышения уровня. Также можно писать аддоны, которые EryBuyer загружает сам из папки `plugins/EryBuyer/addons/`.

[![](https://jitpack.io/v/Agent1k-1/EryBuyer.svg)](https://jitpack.io/#Agent1k-1/EryBuyer)

### 1. Подключение

В `plugin.yml` вашего плагина укажите зависимость, чтобы EryBuyer загрузился раньше:

```yaml
depend: [EryBuyer]
# или, если API необязательно:
softdepend: [EryBuyer]
```

для maven

```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
<groupId>com.github.Agent1k-1</groupId>
    <artifactId>EryBuyer</artifactId>
    <version>v3</version>
    <scope>provided</scope>
</dependency>
```

для gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Agent1k-1:EryBuyer:v3'
}
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

### Настройки автоскупщика

Настройки автоскупщика находятся в обычном `menu/autobuyer.yml`, зарегистрированном в `register-menu`. Отдельная папка и реестр меню `autobuyer` больше не используются. Переход: `[openmenu] autobuyer`. Действия `[autobuyeritems] enable` и `[autobuyeritems] disable` включают или выключают все известные ресурсы для игрока; `[toggleautobuyer]` переключает общий режим. Команда `/autobuyer` сохраняет прежнее поведение.

В обычных разделах скупщика клавиша выброса предмета (по умолчанию Q) переключает автопродажу материала. Предмет не выбрасывается и не продаётся этим нажатием. В повышенном спросе переключение автопродажи на Q отключено; статус предмета доступен для просмотра. Ctrl+Q, смена руки и цифровые клавиши не вызывают продажу. Для изменения фильтра требуется `erybuyer.autobuyer`. Фильтр работает независимо от общего режима автоскупщика, а ручные ЛКМ/ПКМ/Shift-клики сохраняют прежние правила.

Статус и подсказка Q задаются непосредственно в `lore` предметов в `menu/mine.yml`, `menu/farma.yml`, `menu/mob.yml` и в шаблоне предметов `menu/bestmenu.yml`. `%autobuyer_item_status%` заменяется текущим состоянием; тексты состояний задаются в `messages.yml`: `placeholder.enable-item-autobuyer` и `placeholder.disable-item-autobuyer`. Дополнительные строки автоматически не добавляются: их можно менять и удалять в YAML каждого меню. До загрузки фильтра показывается состояние загрузки и автоматическая продажа приостановлена.

Выбор сохраняется отдельно для каждого игрока в SQLite/MySQL и сохраняется после перезапуска. Данные предыдущей версии совместимы. При запуске старый отдельный реестр удаляется из config.yml, новый экран добавляется в обычный register-menu, стандартные старые кнопки перенаправляются в него. Старые файлы на установленном сервере больше не читаются.

| Метод API | Поведение |
|---|---|
| `CompletableFuture<Void> loadAutobuyerSettings(UUID)` | Асинхронно загрузить фильтр; при ошибке future завершается исключением, загрузку можно повторить |
| `boolean isAutobuyerSettingsLoaded(UUID)` | Проверить готовность фильтра |
| `boolean isAutobuyerItemEnabled(UUID, Material)` | Разрешена ли автопродажа материала; до загрузки возвращает `false` |
| `void setAutobuyerItemEnabled(Player, Material, boolean)` | Изменить выбор после успешной загрузки; иначе `IllegalStateException`. Изменение применяется сразу, запись в БД выполняется асинхронно |
| `void openAutobuyerMenu(Player)` | Открыть обычное меню `autobuyer` |

`setAutobuyerItemEnabled` вызывайте в потоке игрока (на Bukkit — в основном потоке, на Folia — в потоке региона игрока). Завершение `loadAutobuyerSettings` может происходить в фоновом потоке; перед изменением настроек переключитесь в поток игрока.

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

[![](https://jitpack.io/v/Agent1k-1/EryBuyer.svg)](https://jitpack.io/#Agent1k-1/EryBuyer)

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

### Оформление скупщика

В главном меню `%buyer_booster_status%` показывает «Нет бустера» на нулевом уровне и текущий уровень бустера после прокачки. Он доступен через встроенные `Placeholders` и расширение PlaceholderAPI `buyer`. `%buyer_factor%` и `%buyer_points%` в меню также работают без PlaceholderAPI.

Шаблон повышенного спроса использует `%custom-item-factor%`, `%custom-item-price-x1%`, `%buyer_limit_now%`, `%buyer_limit_max%` и `%buyer_limit_remaining%`. Переключение автопродажи на Q в этом разделе отключено.

Магазин оформлен в `shop/shop.yml`: товар — временный буст монет за 100 жетонов, кнопка «Обратно» бесплатная. Эффект временного бустера пока не реализован; после покупки временно сохраняется только название из `name` на период `duration-minutes`. `%buyer_booster_name%` используется в `message.booster-buy`, а `%buyer_booster_status%` показывает это название в информации об игроке. Состояние отображения хранится в памяти и сбрасывается после перезапуска сервера. Пакет оформления содержит файлы `menu/` и `shop/shop.yml`. При обновлении существующего сервера сохраните собственные цены и настройки перед заменой YAML.

`%buyer_booster_timer%` показывает остаток времени купленного бустера в формате `мм:сс` (например, `29:59`, без бустера — `00:00`). Повторная покупка того же активного товара отклоняется до списания жетонов и выводит `message.error-booster-buy`; после истечения срока покупка снова доступна. Таймер и название по-прежнему хранятся в памяти до перезапуска, сам эффект временного бустера пока не реализован.

Категории ресурсов: `menu/mob.yml` (лут), `menu/food.yml` (еда), `menu/farma.yml` (фермерство), `menu/mine.yml` (руды). В каждой категории по 8 предметов. `message-name` задаёт название предмета для сообщений (например, «моркови»); при отсутствии поля используется прежнее `name`. Награды существующих предметов сохранены, новым назначены 1 поинт и 1 жетон за штуку — значения можно изменить в YAML.

Настройки `autobuyer-settings` в config.yml больше не используются и удаляются при загрузке старого конфига. Для новых игроков автопродажа предметов разрешена после загрузки персонального фильтра; сохранённые исключения продолжают действовать.

### Проверка обновлений

`check-updates: true` включает однократную асинхронную проверку последнего опубликованного стабильного GitHub Release репозитория `Agent1k-1/EryBuyer` при запуске. При наличии более новой версии в консоли выводятся версия и ссылка. Установка выполняется администратором. При недоступности GitHub плагин продолжает работу; подробности доступны на уровне FINE.

Поддерживаются `v3`, `3.0`, `3.0.1` и старый суффикс `-up`: `v2-up` сравнивается как `2.0.0`, поэтому не считается обновлением для `v3`. Для новых релизов используйте последовательные числовые версии и обновляйте `version` в plugin.yml. Черновики, prerelease и неподдерживаемые форматы не вызывают уведомления. Сетевые тайм-ауты: подключение 5 секунд, запрос 10 секунд.

### CustomModelData

В `item-settings`, `menu-settings` и `knops-settings` можно указать целочисленное поле `custom-model-data`:

```yaml
item-settings:
  custom_diamond:
    material: DIAMOND
    custom-model-data: 1001
    name: '&bОсобый алмаз'
    slot: 20
    prince-x1: 325.0
    prince-x64: 20800.0
    points-from-the-buyer-x1: 15
    tokens-from-the-buyer-x1: 3
```

Модель применяется к предмету в меню. При ручной продаже и автопродаже такой товар принимает только предметы того же материала с указанным CustomModelData. Значение `0` отключает выбор модели. Без поля или при `custom-model-data: 0` сохраняется прежний подбор только по материалу, включая предметы с любой моделью. Не создавайте пересекающиеся товары одного материала без модели и с моделью, если им нужны разные цены: товар без ограничения может принять оба варианта. Персональный фильтр автопродажи остаётся общим для материала. Для отображения модели игроку нужен соответствующий ресурспак; плагин не загружает его автоматически. Настройка не распространяется на отдельный магазин `shop/` и товары повышенного спроса.
