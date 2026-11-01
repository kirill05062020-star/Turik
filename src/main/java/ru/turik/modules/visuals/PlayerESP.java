package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

/**
 * 26. PlayerESP — glowing outlines and fills around players; works
 * through walls via the Turik world shader (depth test off).
 */
public class PlayerESP extends Module {
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", true);
    private final BooleanSetting outline = new BooleanSetting("Outline", true);
    private final BooleanSetting fill = new BooleanSetting("Fill", true);
    private final BooleanSetting glow = new BooleanSetting("Glow", true);
    private final ColorSetting color = new ColorSetting("Color", 0x662BD9FF);
    private final ColorSetting friendColor = new ColorSetting("Friend color", 0x664CE07B);
    private final BooleanSetting colorFriends = new BooleanSetting("Friend color", true);
    private final NumberSetting distance = new NumberSetting("Distance", 8, 128, 1, 64, "m");
    private final NumberSetting lineWidth = new NumberSetting("Line width", 1, 4, 0.5, 1.5);

    public PlayerESP() {
        super("PlayerESP", "Выделение игроков через стены и без препятствий",
                Category.VISUALS);
        add(throughWalls, outline, fill, glow, color, friendColor, colorFriends, distance, lineWidth);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        Render3D.begin(ctx, !throughWalls.get(), lineWidth.getFloat());
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || p.isRemoved() || !p.isAlive() || p.isSpectator()) {
                continue;
            }
            if (mc.player.distanceTo(p) > distance.getFloat()) {
                continue;
            }
            boolean friend = colorFriends.get() && isFriend(p);
            int argb = friend ? friendColor.get() : color.get();
            var box = p.getBoundingBox().expand(0.08);
            int halfAlpha = (argb & 0x00FFFFFF) | (((argb >>> 24) & 0xFF) / 2) << 24;
            int thirdAlpha = (argb & 0x00FFFFFF) | (((argb >>> 24) & 0xFF) / 3) << 24;
            if (glow.get()) {
                var bigger = box.expand(0.06);
                Render3D.boxOutline(bigger.minX, bigger.minY, bigger.minZ,
                        bigger.maxX, bigger.maxY, bigger.maxZ, thirdAlpha);
            }
            if (outline.get()) {
                Render3D.boxOutline(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, argb);
            }
            if (fill.get()) {
                Render3D.boxFill(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, halfAlpha);
            }
        }
        Render3D.end();
    }
}