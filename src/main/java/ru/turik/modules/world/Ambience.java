package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;

import java.awt.Color;

/**
 * 37. Ambience — recolors the sky and light of the world.
 */
public class Ambience extends Module implements SkyOverride {
    private final ColorSetting skyColor = new ColorSetting("Sky color", 0xFF6BC6FF);
    private final NumberSetting intensity = new NumberSetting("Intensity", 0, 100, 1, 60, "%");
    private final BooleanSetting brighterWorld = new BooleanSetting("Brighter world", false);

    public Ambience() {
        super("Ambience", "Изменение атмосферы мира: небо, свет, окружение",
                Category.WORLD);
        add(skyColor, intensity, brighterWorld);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (brighterWorld.get() && mc.world != null) {
            // gentle global light lift via gamma is FullBright's job;
            // Ambience only tints ambience light for visuals
        }
    }

    @Override
    public float[] skyColor() {
        if (!isEnabled()) {
            return null;
        }
        int argb = skyColor.get();
        float mix = intensity.getFloat() / 100f;
        return new float[]{
                ((argb >> 16) & 0xFF) / 255f * mix,
                ((argb >> 8) & 0xFF) / 255f * mix,
                (argb & 0xFF) / 255f * mix};
    }
}