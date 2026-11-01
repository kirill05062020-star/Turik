# Установка Turik

## Из исходников

1. Установите **JDK 21** или новее.
2. Склонируйте проект и выполните:

   ```bash
   ./gradlew build
   ```

3. Готовый мод появится в `build/libs/Turik-1.21.11-1.0.0.jar`.

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