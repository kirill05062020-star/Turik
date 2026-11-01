package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

/** Mouse/crosshair helpers shared by combat and misc modules. */
public final class MouseUtil {
    private MouseUtil() {
    }

    public static PlayerEntity crosshairPlayer(MinecraftClient mc, double maxRange) {
        if (mc.crosshairTarget instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof PlayerEntity player
                    && mc.player.squaredDistanceTo(player) <= maxRange * maxRange) {
                return player;
            }
        }
        return null;
    }
}
