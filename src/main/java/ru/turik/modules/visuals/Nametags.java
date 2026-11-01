package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render2D;
import ru.turik.render.Render3D;
import ru.turik.render.RenderEngine;
import ru.turik.util.PlayerUtils;

/**
 * 28. Nametags — floating plates over players: name, health, armor,
 * distance. Drawn with the Turik typeface through Turik's 2D engine.
 */
public class Nametags extends Module {
    private final NumberSetting size = new NumberSetting("Text size", 5, 14, 0.5, 8);
    private final NumberSetting distance = new NumberSetting("Distance", 8, 128, 1, 64, "m");
    private final BooleanSetting showHealth = new BooleanSetting("Health", true);
    private final BooleanSetting showArmor = new BooleanSetting("Armor", true);
    private final BooleanSetting showDistance = new BooleanSetting("Distance", true);
    private final BooleanSetting colorFriends = new BooleanSetting("Friend color", true);
    private final ColorSetting color = new ColorSetting("Color", 0xFFE8F6FF);
    private final BooleanSetting background = new BooleanSetting("Background", true);

    public Nametags() {
        super("Nametags", "Таблички с именем, здоровьем и бронёй над игроками",
                Category.VISUALS);
        add(size, distance, showHealth, showArmor, showDistance, colorFriends, color, background);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        // 2D overlay pass: own frame, then restore game state.
        RenderEngine engine = RenderEngine.get();
        engine.beginGui(mc);
        float scale = 1f;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || p.isRemoved() || p.isSpectator()) {
                continue;
            }
            double dist = mc.player.distanceTo(p);
            if (dist > distance.getFloat()) {
                continue;
            }
            float[] screen = Render3D.project(p.getPos().add(0, p.getHeight() + 0.45, 0), ctx);
            if (screen == null) {
                continue;
            }
            // Scale plates down with distance for readability.
            float s = (float) Math.max(0.55, 1.0 - dist / 90.0);
            boolean friend = colorFriends.get() && isFriend(p);
            drawTag(mc, p, screen[0], screen[1], size.getFloat() * s, friend, dist);
        }
        engine.endGui(mc);
    }

    private void drawTag(MinecraftClient mc, PlayerEntity p, float cx, float y, float fontSize,
                         boolean friend, double dist) {
        float textH = Render2D.textHeight(fontSize);
        String name = p.getGameProfile().getName();
        int nameColor = friend ? 0xFF4CE07B : color.get();
        float w = Render2D.textWidth(name, fontSize, FontType.SEMIBOLD) + 14;
        if (showHealth.get()) {
            w = Math.max(w, Render2D.textWidth(healthLine(p), fontSize, FontType.REGULAR) + 14);
        }
        float h = textH * (1 + (showHealth.get() ? 1 : 0) + (showArmor.get() || showDistance.get() ? 1 : 0)) + 8;

        float x = cx - w / 2f;
        if (background.get()) {
            Render2D.shadow(x, y, w, h, 5f, 4f, 0x66000000);
            Render2D.roundedRect(x, y, w, h, 5f, 0xC8101216);
        }
        float line = y + 4;
        Render2D.text(name, x + 7, line, fontSize, FontType.SEMIBOLD, nameColor);
        line += textH * 1.15f;
        if (showHealth.get()) {
            Render2D.text(healthLine(p), x + 7, line, fontSize, FontType.REGULAR, 0xFFFF6B6B);
            line += textH * 1.15f;
        }
        if (showArmor.get() && showDistance.get()) {
            Render2D.text(String.format("BR %d  ·  %.1fм", (int) p.getArmor(), dist),
                    x + 7, line, fontSize, FontType.REGULAR, 0xFF9FC8E8);
        } else if (showArmor.get()) {
            Render2D.text("BR " + (int) p.getArmor(), x + 7, line, fontSize, FontType.REGULAR, 0xFF9FC8E8);
        } else if (showDistance.get()) {
            Render2D.text(String.format("%.1fм", dist), x + 7, line, fontSize, FontType.REGULAR, 0xFF9FC8E8);
        }
    }

    private String healthLine(PlayerEntity p) {
        double hp = PlayerUtils.health(p);
        double max = PlayerUtils.maxHealth(p);
        return String.format("%.0f / %.0f HP", hp, max);
    }
}