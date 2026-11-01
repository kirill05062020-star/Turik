package ru.turik.modules.misc;

import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;

import java.util.List;

/**
 * 48. Friends — the FriendManager module: the visible face of the shared
 * friend list. All friend logic lives in ru.turik.friend.FriendManager;
 * this module exposes its color and feedback settings and can display
 * the list as a notification.
 */
public class Friends extends Module {
    private final ColorSetting color = new ColorSetting("Friend color", 0xFF4CE07B);
    private final BooleanSetting showListOnEnable = new BooleanSetting("Show list on enable", true);

    public Friends() {
        super("Friends", "Единая система управления друзьями",
                Category.MISC);
        add(color, showListOnEnable);
    }

    @Override
    public void onEnable() {
        Turik.get().friends().setFriendColor(color.get());
        if (showListOnEnable.get()) {
            List<String> list = Turik.get().friends().list();
            Turik.get().notifications().info("Друзья",
                    list.isEmpty() ? "Список пуст — PC-клик по игроку добавит его"
                            : "Друзья: " + String.join(", ", list));
        }
    }

    @Override
    public void onDisable() {
        // The friend list itself stays active — it's a core subsystem.
    }
}