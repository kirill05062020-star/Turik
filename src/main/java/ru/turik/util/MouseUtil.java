package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/** Mouse/crosshair helpers shared by combat and misc modules. */
public final class MouseUtil {
    private MouseUtil() {
    }

    public static PlayerEntity crosshairPlayer(MinecraftClient mc, double maxRange) {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == net.minecraft.hit.EntityHitResult.ENTITY
                && mc.crosshairTarget instanceof net.minecraft.entity.EntityHitResult hit) {
            if (hit.getEntity() instanceof PlayerEntity player
                    && mc.player.squaredDistanceTo(player) <= maxRange * maxRange) {
                return player;
            }
        }
        return null;
    }
}
