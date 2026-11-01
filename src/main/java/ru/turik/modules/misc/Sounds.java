package ru.turik.modules.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 44. Sounds — client sound feedback on Turik events (module toggles,
 * friends, notifications).
 */
public class Sounds extends Module {
    private final NumberSetting volume = new NumberSetting("Volume", 0, 100, 5, 40, "%");
    private final BooleanSetting onToggle = new BooleanSetting("Module toggle", true);
    private final BooleanSetting onFriend = new BooleanSetting("Friend change", true);
    private final BooleanSetting onError = new BooleanSetting("Notifications", true);

    public Sounds() {
        super("Sounds", "Звуковые уведомления о событиях клиента",
                Category.MISC);
        add(volume, onToggle, onFriend, onError);
    }

    public void play(SoundEvent event, boolean condition) {
        if (!isEnabled() || !condition) {
            return;
        }
        MinecraftClient mc = Turik.get().mc();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(PositionedSoundInstance.master(event,
                    1.0f, volume.get() / 100f));
        }
    }

    public void moduleToggled() {
        play(SoundEvents.UI_BUTTON_CLICK.value(), onToggle.get());
    }

    public void friendChanged() {
        play(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, onFriend.get());
    }

    public void notification() {
        play(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), onError.get());
    }
}