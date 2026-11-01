package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.mixin.WorldInvoker;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 39. Weather — client-side weather visuals: clear, rain, thunder with
 * an intensity slider.
 */
public class Weather extends Module {
    private final ModeSetting weather = new ModeSetting("Weather", "Clear", "Rain", "Thunder");
    private final NumberSetting intensity = new NumberSetting("Intensity", 0, 100, 5, 60, "%");
    private final BooleanSetting thunderFlash = new BooleanSetting("Thunder flashes", true);

    public Weather() {
        super("Weather", "Визуальное изменение погоды",
                Category.WORLD);
        add(weather, intensity, thunderFlash);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.world == null || !(mc.world instanceof WorldInvoker invoker)) {
            return;
        }
        float target = switch (weather.get()) {
            case "Rain" -> intensity.getFloat() / 100f * 0.7f;
            case "Thunder" -> intensity.getFloat() / 100f;
            default -> 0f;
        };
        invoker.turik$setRainGradient(target);
        if (weather.is("Thunder") && thunderFlash.get()) {
            mc.world.setLightningTicksLeft(1);
        }
    }
}