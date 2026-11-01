package ru.turik.modules.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import ru.turik.Turik;
import ru.turik.mixin.WorldInvoker;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

import java.util.Random;

/**
 * 38. Atmosphere — four full presets: Winter (snow), Autumn (leaves),
 * Mars (red sky, dust), Saturn (cosmic sky, ring particles).
 * Each preset recolors the sky, drives precipitation and spawns its own
 * GPU-rendered particles through the Turik world renderer.
 */
public class Atmosphere extends Module implements SkyOverride {
    private final ModeSetting preset = new ModeSetting("Preset", "Winter", "Autumn", "Mars", "Saturn");
    private final NumberSetting intensity = new NumberSetting("Intensity", 0, 100, 1, 70, "%");
    private final NumberSetting particleCount = new NumberSetting("Particles", 20, 300, 10, 120, "шт");
    private final ColorSetting snowColor = new ColorSetting("Snow color", 0xE8FFFFFF);
    private final ColorSetting leafColor = new ColorSetting("Leaf color", 0xE8E08A2E);

    private static final float[][] SKY = {
            {0.55f, 0.72f, 0.95f},  // Winter
            {0.85f, 0.62f, 0.25f},  // Autumn
            {0.72f, 0.35f, 0.15f},  // Mars
            {0.08f, 0.07f, 0.22f},  // Saturn
    };

    public Atmosphere() {
        super("Atmosphere", "Атмосферные пресеты: Winter, Autumn, Mars, Saturn",
                Category.WORLD);
        add(preset, intensity, particleCount, snowColor, leafColor);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.world == null) {
            return;
        }
        if (mc.world instanceof WorldInvoker invoker) {
            // Winter wants visible snow; others clear vanilla precipitation.
            boolean winter = preset.is("Winter");
            boolean autumn = preset.is("Autumn");
            if (winter && !mc.world.isSnowing()) {
                invoker.turik$setRainGradient(intensity.getFloat() / 200f);
            } else if (!winter && !autumn && mc.world.isRaining()) {
                invoker.turik$setRainGradient(0f);
            }
        }
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        boolean winter = preset.is("Winter");
        boolean autumn = preset.is("Autumn");
        boolean mars = preset.is("Mars");
        boolean saturn = preset.is("Saturn");
        if (!winter && !autumn && !mars && !saturn) {
            return;
        }

        long seed = mc.world.getTime();
        Render3D.begin(ctx, true, 2f);
        Random rnd = new Random(seed / 40);
        int n = particleCount.getInt();
        double radius = 28;
        Vec3d cam = mc.player.getPos();
        for (int i = 0; i < n; i++) {
            double t = ((seed % 3600) / 3600.0) + rnd.nextDouble();
            double ang = rnd.nextDouble() * Math.PI * 2;
            double x = cam.x + Math.cos(ang) * radius;
            double z = cam.z + Math.sin(ang) * radius;
            double y = cam.y + (t % 1.0) * 24 - 4;
            if (winter) {
                y = cam.y + 14 - (t % 1.0) * 22;
                Render3D.boxOutline(x, y, z, x + 0.06, y + 0.06, z + 0.06, snowColor.get());
            } else if (autumn) {
                y = cam.y + 16 - (t % 1.0) * 26;
                Render3D.boxOutline(x, y, z, x + 0.12, y + 0.02, z + 0.12, leafColor.get());
            } else if (mars) {
                y = cam.y + 10 - (t % 1.0) * 18;
                Render3D.boxOutline(x, y, z, x + 0.05, y + 0.05, z + 0.05, 0x60D98C4A);
            } else if (saturn) {
                // ring of slowly drifting motes high above the player
                y = cam.y + 20 + Math.sin(t * Math.PI) * 6;
                Render3D.boxOutline(x, y, z, x + 0.08, y + 0.08, z + 0.08, 0x50C8B8FF);
            }
        }
        Render3D.end();
    }

    @Override
    public float[] skyColor() {
        if (!isEnabled()) {
            return null;
        }
        float mix = intensity.getFloat() / 100f;
        float[] rgb = switch (preset.get()) {
            case "Winter" -> SKY[0];
            case "Autumn" -> SKY[1];
            case "Mars" -> SKY[2];
            case "Saturn" -> SKY[3];
            default -> null;
        };
        if (rgb == null) {
            return null;
        }
        return new float[]{rgb[0] * mix, rgb[1] * mix, rgb[2] * mix};
    }
}