package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

/**
 * 29. ItemESP — highlights dropped items on the ground.
 * Modes: Visible ESP (depth-tested) and Through Walls ESP.
 */
public class ItemESP extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Through Walls", "Visible");
    private final BooleanSetting outline = new BooleanSetting("Outline", true);
    private final BooleanSetting fill = new BooleanSetting("Fill", true);
    private final ColorSetting color = new ColorSetting("Color", 0x80FFD166);
    private final NumberSetting alpha = new NumberSetting("Fill alpha", 0, 100, 5, 30, "%");
    private final NumberSetting distance = new NumberSetting("Distance", 8, 96, 1, 48, "m");
    private final NumberSetting lineWidth = new NumberSetting("Line width", 1, 3, 0.5, 1);

    public ItemESP() {
        super("ItemESP", "Подсветка предметов на земле",
                Category.VISUALS);
        add(mode, outline, fill, color, alpha, distance, lineWidth);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        boolean throughWalls = mode.is("Through Walls");
        int argb = color.get();
        int fillArgb = (argb & 0x00FFFFFF) | (int) (alpha.get() / 100.0 * 255) << 24;
        Render3D.begin(ctx, !throughWalls, lineWidth.getFloat());
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity item) || item.isRemoved()) {
                continue;
            }
            if (mc.player.distanceTo(item) > distance.getFloat()) {
                continue;
            }
            var box = item.getBoundingBox().expand(0.05);
            if (outline.get()) {
                Render3D.boxOutline(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, argb);
            }
            if (fill.get()) {
                Render3D.boxFill(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, fillArgb);
            }
        }
        Render3D.end();
    }
}