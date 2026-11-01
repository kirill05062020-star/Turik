package ru.turik.modules.misc;

import net.minecraft.entity.player.PlayerEntity;
import ru.turik.Turik;
import ru.turik.event.Listener;
import ru.turik.event.events.Events;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.util.MouseUtil;

/**
 * 45. MiddleClickFriend — middle-click a player to add/remove them from
 * the shared friend list.
 */
public class MiddleClickFriend extends Module {
    private final BooleanSetting notify = new BooleanSetting("Notify", true);
    private final BooleanSetting onlyPlayers = new BooleanSetting("Only players", true);

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Добавление/удаление друзей средней кнопкой мыши",
                Category.MISC);
        add(notify, onlyPlayers);
    }

    @Listener
    public void onMouse(Events.MouseButtonEvent event) {
        if (!isEnabled() || event.button() != 2 || !event.isPress()) {
            return;
        }
        var mc = Turik.get().mc();
        PlayerEntity target = MouseUtil.crosshairPlayer(mc, 64);
        if (target == null) {
            return;
        }
        String name = target.getGameProfile().getName();
        boolean added = Turik.get().friends().toggle(name);
        if (notify.get()) {
            Turik.get().notifications().info("Друзья",
                    name + (added ? " добавлен в друзья" : " удалён из друзей"));
        }
        Turik.get().modules().get(Sounds.class).friendChanged();
    }
}