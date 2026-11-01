package ru.turik.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.render.Render3D;

/**
 * 36. Trajectories — simulates the arc of arrows, snowballs, eggs, pearls
 * and tridents and marks the landing point.
 */
public class Trajectories extends Module {
    private final ColorSetting pathColor = new ColorSetting("Path color", 0x992BD9FF);
    private final ColorSetting landingColor = new ColorSetting("Landing color", 0xC8FF5252);
    private final BooleanSetting showLanding = new BooleanSetting("Landing point", true);

    public Trajectories() {
        super("Trajectories", "Траектория стрел, снежков и метательных предметов",
                Category.VISUALS);
        add(pathColor, landingColor, showLanding);
    }

    @Override
    public void onWorldRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || mc.world == null) {
            return;
        }
        Item item = mc.player.getMainHandStack().getItem();
        if (item != Items.BOW && item != Items.CROSSBOW && item != Items.ENDER_PEARL
                && item != Items.SNOWBALL && item != Items.EGG && item != Items.TRIDENT
                && item != Items.SPLASH_POTION) {
            return;
        }
        // Launch velocity (simplified vanilla-like constants).
        double power = item == Items.BOW && mc.player.isUsingItem()
                ? 1.0 + mc.player.getItemUseTime() / 20.0 * 0.5
                : (item == Items.BOW ? 1.5 : 1.5);
        power = Math.min(3.0, power);
        Vec3d vel = Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw()).multiply(power * 0.9);
        Vec3d pos = mc.player.getEyePos();

        Render3D.begin(ctx, false, 1.5f);
        Vec3d prev = pos;
        Vec3d land = null;
        for (int i = 0; i < 100; i++) {
            pos = pos.add(vel);
            vel = new Vec3d(vel.x * 0.99, vel.y * 0.99 - 0.05, vel.z * 0.99);
            BlockHitResult hit = null;
            var clip = mc.world.raycast(new net.minecraft.world.RaycastContext(
                    prev, pos, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player));
            if (clip.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                land = clip.getPos();
                Render3D.line(prev, clip.getPos(), pathColor.get());
                break;
            }
            Render3D.line(prev, pos, pathColor.get());
            prev = pos;
        }
        if (showLanding.get() && land != null) {
            BlockPos bp = BlockPos.ofFloored(land);
            Render3D.boxOutline(bp.getX() - 0.05, bp.getY(), bp.getZ() - 0.05,
                    bp.getX() + 1.05, bp.getY() + 0.05, bp.getZ() + 1.05, landingColor.get());
        }
        Render3D.end();
    }
}