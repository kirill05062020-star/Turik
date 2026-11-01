# Установка Turik

## Компиляция из исходников

### Шаг 1. Окружение
- Установите **JDK 21** (например, [Adoptium Temurin](https://adoptium.net/)).
- Установите **IntelliJ IDEA Community** (проще всего) и/или **Gradle** 9.x.

### Шаг 2. Открытие проекта
1. В IntelliJ IDEA: **File → Open**, выберите папку проекта (Turik Client).
2. IDEA определит `build.gradle`, скачает Gradle 9.8.0 (из `gradle/wrapper/gradle-wrapper.properties`),
   Loom 1.19.3, Minecraft 1.21.11, yarn-маппинги и Fabric API — первый Sync занимает несколько минут.
3. Если Sync сообщил об ошибках компиляции — исправьте их по подсказкам IDE;
   вероятные места перечислены в `docs/LIMITATIONS.md` (п.2 — имена методов yarn).

### Шаг 3. Сборка
- В IDEA: Gradle-панель → **turik → build → build**.
- Или в терминале из папки проекта: `gradle wrapper && ./gradlew build`.

Готовый мод: `build/libs/Turik-1.21.11-1.0.0.jar`.

## Установка в Minecraft

1. Установите **Fabric Loader 0.20.1+** для Minecraft **1.21.11**
   (через [Fabric-установщик](https://fabricmc.net/use/installer/)).
2. Скачайте **Fabric API 0.141.6+1.21.11** (Modrinth/CurseForge)
   и положите jar в папку `.minecraft/mods`.
3. Скопируйте `Turik-1.21.11-1.0.0.jar` в ту же папку `.minecraft/mods`.
4. Запустите игру с профилем **fabric-loader-1.21.11**.

## Управление

- **Right Shift** — открыть/закрыть ClickGUI.
- **Правый клик** по модулю в GUI — развернуть настройки.
- Кнопка **Configs** — профили конфигурации (создание/загрузка/удаление).
- Кнопка **HUD Editor** — перетаскивание элементов HUD мышью.
- Клавиши модулей настраиваются в настройках каждого модуля (Bind).
- **Средняя кнопка мыши** по игроку — добавить/убрать друга (MiddleClickFriend).

## Файлы клиента

- `.minecraft/turik/friends.json` — список друзей.
- `.minecraft/turik/configs/active.json` — активная конфигурация.
- `.minecraft/turik/configs/*.json` — профили.