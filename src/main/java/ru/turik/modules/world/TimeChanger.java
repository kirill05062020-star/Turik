package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 40. TimeChanger — overrides the displayed time of day.
 */
public class TimeChanger extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Day", "Night", "Sunrise", "Sunset", "Manual");
    private final NumberSetting manualTime = new NumberSetting("Manual time", 0, 24000, 100, 12000, "т");

    public TimeChanger() {
        super("TimeChanger", "Изменение отображаемого времени суток",
                Category.WORLD);
        add(mode, manualTime);
    }

    /** Called by the ClientWorld mixin; null keeps vanilla time. */
    public Float timeOverride() {
        if (!isEnabled()) {
            return null;
        }
        return switch (mode.get()) {
            case "Day" -> 6000f;
            case "Night" -> 18000f;
            case "Sunrise" -> 23000f;
            case "Sunset" -> 13000f;
            case "Manual" -> (float) manualTime.get();
            default -> null;
        };
    }

    @Override
    public void onDisable() {
        // nothing to restore: the override is inactive when disabled
    }
}