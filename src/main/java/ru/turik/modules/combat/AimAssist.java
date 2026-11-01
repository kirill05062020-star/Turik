package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.Rotations;
import ru.turik.util.TickTimer;

import java.util.Comparator;
import java.util.List;

/**
 * 1. AimAssist — smooth crosshair pull toward the nearest enemy inside the
 * search FOV. Never targets friends (shared FriendManager).
 */
public class AimAssist extends Module {
    private final NumberSetting strength = new NumberSetting("Strength", 0, 100, 1, 60, "%");
    private final NumberSetting radius = new NumberSetting("Search radius", 5, 90, 1, 40, "°");
    private final NumberSetting turnSpeed = new NumberSetting("Turn speed", 10, 100, 1, 35, "°/t");
    private final NumberSetting range = new NumberSetting("Range", 2, 6.5, 0.1, 4.2, "m");
    private final NumberSetting heightFactor = new NumberSetting("Aim part", 0, 1, 0.05, 0.8);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore friends", true);
    private final BooleanSetting visibleOnly = new BooleanSetting("Visible only", true);

    private final TickTimer reTarget = new TickTimer();

    public AimAssist() {
        super("AimAssist", "Помощь в наведении прицела на ближайшую цель",
                Category.COMBAT);
        add(strength, radius, turnSpeed, range, heightFactor, ignoreFriends, visibleOnly);
        // Aim part: 0.05 = head, 1.0 = feet; slider exposed in the GUI
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || strength.get() <= 0) {
            return;
        }
        PlayerEntity target = findTarget(mc);
        if (target == null) {
            return;
        }
        float[] want = Rotations.lookAt(mc.player.getEyePos(),
                Rotations.aimPoint(target, heightFactor.get()));
        float maxDeg = turnSpeed.getFloat() * (strength.getFloat() / 100f);
        float[] smoothed = Rotations.smooth(
                new float[]{mc.player.getYaw(), mc.player.getPitch()}, want, maxDeg);
        Rotations.apply(mc, smoothed);
    }

    private PlayerEntity findTarget(MinecraftClient mc) {
        List<PlayerEntity> players = mc.world.getPlayers();
        PlayerEntity best = null;
        float bestAngle = Float.MAX_VALUE;
        for (PlayerEntity p : players) {
            if (p == mc.player || p.isRemoved() || !p.isAlive() || p.isSpectator()) {
                continue;
            }
            if (ignoreFriends.get() && isFriend(p)) {
                continue;
            }
            double dist = mc.player.distanceTo(p);
            if (dist > range.get()) {
                continue;
            }
            if (visibleOnly.get() && !mc.player.canSee(p)) {
                continue;
            }
            float angle = Rotations.angleTo(mc, Rotations.aimPoint(p, heightFactor.get()));
            if (angle > radius.getFloat()) {
                continue;
            }
            if (angle < bestAngle) {
                bestAngle = angle;
                best = p;
            }
        }
        return best;
    }
}
