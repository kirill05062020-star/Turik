# Turik Client

**Turik** — самостоятельный клиент для Minecraft Java Edition **1.21.11** на **Fabric**.
Собственная архитектура, собственный ClickGUI, собственный HUD, собственная система
конфигураций, собственный GLSL-шейдерный движок и собственная система шрифтов
(Manrope, поддержка кириллицы). 47 игровых функций.

## Состав

| Подсистема | Описание |
|---|---|
| События | `event` — собственная типизированная шина событий + Fabric lifecycle |
| Модули | `module` + `modules/*` — 47 модулей в 6 категориях, реестр, клавиши |
| Друзья | `friend` — единый FriendManager (сохраняется, общий для всех модулей) |
| Шрифты | `font` — TTF-растеризация через Java2D + GL-текстуры, 4 начертания |
| Рендер | `render` — собственный OpenGL-движок: батчи, SDF-скругления, blur, world-space |
| Шейдеры | GLSL-программы в `resources/assets/turik/shaders` + ShaderManager |
| ClickGUI | `gui` — поиск, категории, свитчи, слайдеры, режимы, цвета, клавиши, профили |
| HUD | `hud` — Watermark, координаты, активные модули, drag-редактор, уведомления |
| Конфиги | `config` — профили, автосохранение, восстановление повреждённых файлов |
| Mixins | `mixin` — точечные инъекции (NoSlow, Velocity, NoFall, FastBreak, погода...) |

## Сборка

Требуется JDK 21+. Проект подключён к Gradle 9.1 через `gradle/wrapper/gradle-wrapper.properties`.

**Вариант А — через IntelliJ IDEA (рекомендуется):**
1. Установите [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/) и JDK 21.
2. File → Open → выберите папку проекта — IDEA сама распознает `build.gradle`,
   скачает Gradle 9.1 (по wrapper-конфигу), Loom, Minecraft, yarn и Fabric API.
3. Дождитесь окончания Gradle Sync (индексация займет несколько минут при первом открытии).
4. Если Sync/компиляция нашла ошибки — правьте по подсказкам IDE (список
   вероятных мест — в `docs/LIMITATIONS.md`, п.2).
5. Gradle-панель → turik → build → **build**. Готовый jar — в `build/libs/`.

**Вариант Б — через терминал:**
1. Установите Gradle (`brew install gradle` / [gradle.org/install](https://gradle.org/install/)) и JDK 21.
2. В папке проекта: `gradle wrapper` (сгенерирует `gradlew` по wrapper-конфигу), затем `./gradlew build`.
3. Готовый jar появится в `build/libs/Turik-1.21.11-1.0.0.jar`.

Версии (проверены по официальным источникам):
- Minecraft **1.21.11**
- Fabric Loader **0.20.1**
- Fabric API **0.141.6+1.21.11**
- Yarn mappings **1.21.11+build.6**
- Fabric Loom **1.19.3**

Подробности установки: `INSTALL.md`.
Отчёт о тестировании: `docs/TESTING.md`.
Известные ограничения: `docs/LIMITATIONS.md`.
Описание всех 47 функций: `docs/MODULES.md`.

## Лицензия

MIT. Шрифт Manrope — SIL Open Font License (включён в ресурсы).