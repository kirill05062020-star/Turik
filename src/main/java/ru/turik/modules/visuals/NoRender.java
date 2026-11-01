package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.mixin.WorldInvoker;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 32. NoRender — suppresses selected vanilla visuals (fire overlay,
 * rain, snow, heavy particles).
 */
public class NoRender extends Module {
    private final BooleanSetting fire = new BooleanSetting("Fire overlay", true);
    private final BooleanSetting rain = new BooleanSetting("Rain", true);
    private final BooleanSetting snow = new BooleanSetting("Snow", true);
    private final BooleanSetting explosionParticles = new BooleanSetting("Explosion particles", true);

    public NoRender() {
        super("NoRender", "Отключает выбранные визуальные эффекты игры",
                Category.VISUALS);
        add(fire, rain, snow, explosionParticles);
    }

    public boolean hideFire() {
        return isEnabled() && fire.get();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.world == null) {
            return;
        }
        boolean killPrecip = (rain.get() || snow.get()) && mc.world.isRaining();
        if (killPrecip && mc.world instanceof WorldInvoker invoker) {
            invoker.turik$setRainGradient(0f);
        }
    }
}