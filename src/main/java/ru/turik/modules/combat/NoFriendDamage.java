package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;

/**
 * 7. NoFriendDamage — blocks every attack (manual, TriggerBot, AimAssist
 * driven) on friends. Uses the shared FriendManager; nothing to configure
 * except the friend color and feedback.
 */
public class NoFriendDamage extends Module {
    private final BooleanSetting alsoCancelCrit = new BooleanSetting("Also block fishing rod", false);
    private final BooleanSetting notifyOnBlock = new BooleanSetting("Notify when blocked", true);
    private final ColorSetting friendColor = new ColorSetting("Friend color", 0xFF4CE07B);

    public NoFriendDamage() {
        super("NoFriendDamage", "Защита друзей от ваших атак",
                Category.COMBAT);
        add(alsoCancelCrit, notifyOnBlock, friendColor);
    }

    /** Called from the interaction-manager mixin before any attack lands. */
    public boolean blocked(Entity target) {
        if (!isEnabled() || !(target instanceof PlayerEntity player)) {
            return false;
        }
        boolean friend = Turik.get().friends().isFriend(player);
        if (friend && notifyOnBlock.get()) {
            Turik.get().notifications().warn("NoFriendDamage",
                    "Атака по другу " + player.getGameProfile().getName() + " отменена");
        }
        return friend;
    }

    @Override
    public void onEnable() {
        Turik.get().friends().setFriendColor(friendColor.get());
    }
}
