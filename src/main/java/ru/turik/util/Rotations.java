package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Rotation math for AimAssist, TriggerBot (Smart) and Trajectories. */
public final class Rotations {
    private Rotations() {
    }

    /** Yaw/pitch (degrees) needed to look from the player's eyes to a point. */
    public static float[] lookAt(Vec3d eyes, Vec3d target) {
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[]{yaw, pitch};
    }

    /** Point on an entity's bounding box: head, body or feet. */
    public static Vec3d aimPoint(Entity entity, double heightFactor) {
        double h = entity.getBoundingBox().getLengthY();
        double body = entity.getY() + MathHelper.clamp(heightFactor, 0d, 1d) * h;
        return new Vec3d(entity.getX(), body, entity.getZ());
    }

    /** Moves current yaw/pitch toward target by at most maxDegrees. */
    public static float[] smooth(float[] current, float[] target, float maxDegrees) {
        float dYaw = MathHelper.wrapDegrees(target[0] - current[0]);
        float dPitch = MathHelper.wrapDegrees(target[1] - current[1]);
        float yaw = current[0] + MathHelper.clamp(dYaw, -maxDegrees, maxDegrees);
        float pitch = current[1] + MathHelper.clamp(dPitch, -maxDegrees, maxDegrees);
        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
    }

    /** Angular distance (degrees) between the view direction and a point. */
    public static float angleTo(MinecraftClient mc, Vec3d point) {
        float[] need = lookAt(mc.player.getEyePos(), point);
        float dYaw = MathHelper.wrapDegrees(need[0] - mc.player.getYaw());
        float dPitch = MathHelper.wrapDegrees(need[1] - mc.player.getPitch());
        return (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    /** Applies rotations to the local player without packets (server-agnostic). */
    public static void apply(MinecraftClient mc, float[] rot) {
        mc.player.setYaw(rot[0]);
        mc.player.setPitch(rot[1]);
    }
}
