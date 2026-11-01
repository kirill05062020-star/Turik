package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

/**
 * 35. EntityESP — highlights mobs and other living entities with filters
 * and separate colors.
 */
public class EntityESP extends Module {
    private final ModeSetting filter = new ModeSetting("Filter", "All", "Hostile", "Passive");
    private final ColorSetting hostileColor = new ColorSetting("Hostile color", 0xA0FF5252);
    private final ColorSetting passiveColor = new ColorSetting("Passive color", 0xA04CE07B);
    private final BooleanSetting outline = new BooleanSetting("Outline", true);
    private final BooleanSetting fill = new BooleanSetting("Fill", true);
    private final NumberSetting distance = new NumberSetting("Distance", 8, 128, 1, 64, "m");
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", true);

    public EntityESP() {
        super("EntityESP", "Выделение мобов и других сущностей",
                Category.VISUALS);
        add(filter, hostileColor, passiveColor, outline, fill, distance, throughWalls);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        Render3D.begin(ctx, !throughWalls.get(), 1.2f);
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity l) || e == mc.player || e.isRemoved() || e.isSpectator()) {
                continue;
            }
            boolean hostile = l instanceof HostileEntity;
            if (filter.is("Hostile") && !hostile) {
                continue;
            }
            if (filter.is("Passive") && hostile) {
                continue;
            }
            if (mc.player.distanceTo(e) > distance.getFloat()) {
                continue;
            }
            int argb = hostile ? hostileColor.get() : passiveColor.get();
            var box = e.getBoundingBox().expand(0.05);
            int fillArgb = (argb & 0x00FFFFFF) | (((argb >>> 24) & 0xFF) / 2) << 24;
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