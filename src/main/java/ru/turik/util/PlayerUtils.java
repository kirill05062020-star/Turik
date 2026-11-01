package ru.turik.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

/** Player-facing info helpers used by the HUD and nametags. */
public final class PlayerUtils {
    private PlayerUtils() {
    }

    public static int fps(MinecraftClient mc) {
        return mc.getCurrentFps();
    }

    public static int ping(PlayerEntity player) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }

    public static double health(PlayerEntity player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    public static double maxHealth(PlayerEntity player) {
        return player.getMaxHealth() + player.getAbsorptionAmount();
    }

    public static double armorPoints(PlayerEntity player) {
        return player.getArmor();
    }

    public static double distance(PlayerEntity from, PlayerEntity to) {
        return from.distanceTo(to);
    }
}
