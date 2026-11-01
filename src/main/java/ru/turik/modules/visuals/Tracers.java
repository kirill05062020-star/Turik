package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.render.Render3D;

/**
 * 33. Tracers — lines from the player to chosen entities.
 */
public class Tracers extends Module {
    private final ModeSetting target = new ModeSetting("Target", "Players", "Hostile", "All");
    private final ColorSetting color = new ColorSetting("Color", 0x992BD9FF);
    private final NumberSetting lineWidth = new NumberSetting("Line width", 0.5, 4, 0.5, 1.5);
    private final NumberSetting distance = new NumberSetting("Distance", 8, 128, 1, 64, "m");
    private final BooleanSetting toFeet = new BooleanSetting("To feet", false);

    public Tracers() {
        super("Tracers", "Линии от игрока к сущностям",
                Category.VISUALS);
        add(target, color, lineWidth, distance, toFeet);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        Render3D.begin(ctx, false, lineWidth.getFloat());
        Vec3d from = mc.player.getEyePos();
        for (Entity e : mc.world.getEntities()) {
            if (!matches(mc, e)) {
                continue;
            }
            if (mc.player.distanceTo(e) > distance.getFloat()) {
                continue;
            }
            Vec3d to = toFeet.get() ? e.getPos() : e.getPos().add(0, e.getHeight() / 2.0, 0);
            Render3D.line(from, to, color.get());
        }
        Render3D.end();
    }

    private boolean matches(MinecraftClient mc, Entity e) {
        if (e == mc.player || e.isRemoved() || e.isSpectator()) {
            return false;
        }
        switch (target.get()) {
            case "Players" -> {
                return e instanceof PlayerEntity p && !isFriend(p);
            }
            case "Hostile" -> {
                return e instanceof LivingEntity l && l.isHostile();
            }
            case "All" -> {
                if (e instanceof PlayerEntity p && isFriend(p)) {
                    return false;
                }
                return e instanceof LivingEntity;
            }
            default -> {
                return false;
            }
        }
    }
}