package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render2D;
import ru.turik.render.RenderEngine;

/**
 * 41. Fog — atmospheric haze through Turik's own sky shader: a
 * screen-space horizon blend with configurable density and color.
 */
public class Fog extends Module {
    private final NumberSetting density = new NumberSetting("Density", 0, 100, 1, 30, "%");
    private final NumberSetting horizon = new NumberSetting("Horizon height", 0.2, 0.9, 0.05, 0.55);
    private final ColorSetting color = new ColorSetting("Color", 0xFF9FC8E8);
    private final BooleanSetting showLabel = new BooleanSetting("Label", false);

    public Fog() {
        super("Fog", "Настройка тумана: плотность, цвет, дистанция",
                Category.WORLD);
        add(density, horizon, color, showLabel);
    }

    /** Renders the haze; called from the HUD render pass after the world. */
    public void renderHud(float tickDelta) {
        if (!isEnabled() || density.get() <= 0) {
            return;
        }
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.options.getFov().getValue() <= 0) {
            return;
        }
        RenderEngine engine = RenderEngine.get();
        if (!engine.inFrame()) {
            engine.beginGui(mc);
            int argb = color.get();
            int alpha = (int) (density.get() / 100.0 * 140);
            int fogArgb = (alpha << 24) | (argb & 0x00FFFFFF);
            float w = engine.viewWidth() / engine.guiScale();
            float h = engine.viewHeight() / engine.guiScale();
            int transparent = (argb & 0x00FFFFFF);
            // Fog band: transparent at the top, dense at the screen bottom.
            float bandStart = h * (1f - horizon.getFloat());
            engine.gradientV(0, bandStart, w, h - bandStart, transparent, fogArgb);
            if (showLabel.get()) {
                Render2D.text(String.format("Fog %d%%", density.getInt()), 12, h - 28, 9,
                        FontType.MEDIUM, fogArgb);
            }
            engine.endGui(mc);
        }
    }
}