package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 31. FullBright — pushes gamma past the vanilla maximum for darkness
 * visibility, restoring the original value on disable.
 */
public class FullBright extends Module {
    private final NumberSetting brightness = new NumberSetting("Brightness", 1, 16, 0.5, 10);
    private final BooleanSetting smooth = new BooleanSetting("Smooth transition", true);

    private double originalGamma = 1.0;
    private boolean stored;

    public FullBright() {
        super("FullBright", "Улучшает видимость в темноте",
                Category.VISUALS);
        add(brightness, smooth);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = Turik.get().mc();
        if (!stored) {
            originalGamma = mc.options.getGamma().getValue();
            stored = true;
        }
    }

    @Override
    public void onDisable() {
        if (stored) {
            Turik.get().mc().options.getGamma().setValue(originalGamma);
            stored = false;
        }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) {
            return;
        }
        double target = brightness.get();
        double current = mc.options.getGamma().getValue();
        if (smooth.get()) {
            double next = current + (target - current) * 0.08;
            mc.options.getGamma().setValue(next);
        } else {
            mc.options.getGamma().setValue(target);
        }
    }
}