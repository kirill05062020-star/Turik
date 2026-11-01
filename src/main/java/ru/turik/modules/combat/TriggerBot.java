package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.MouseUtil;
import ru.turik.util.TickTimer;

/**
 * 2. TriggerBot — attacks the entity currently under the crosshair.
 * Modes: Normal / Only Crits / Smart.
 */
public class TriggerBot extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Only Crits", "Smart");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 10, 120, "ms");
    private final NumberSetting range = new NumberSetting("Range", 2, 6.5, 0.1, 4.5, "m");
    private final BooleanSetting checkVisibility = new BooleanSetting("Check visibility", true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore friends", true);
    private final BooleanSetting respectCooldown = new BooleanSetting("Respect attack cooldown", true);

    private final TickTimer timer = new TickTimer();
    private boolean charged;

    public TriggerBot() {
        super("TriggerBot", "Автоматическая атака цели под прицелом",
                Category.COMBAT);
        add(mode, delay, range, checkVisibility, ignoreFriends, respectCooldown);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.player.isUsingItem()) {
            return;
        }
        PlayerEntity target = MouseUtil.crosshairPlayer(mc, range.get());
        if (target == null) {
            charged = false;
            return;
        }
        if (ignoreFriends.get() && isFriend(target)) {
            charged = false;
            return;
        }
        if (checkVisibility.get() && !mc.player.canSee(target)) {
            charged = false;
            return;
        }
        if (!readyToAttack(mc, target)) {
            charged = false;
            return;
        }
        // Delay is satisfied only after the target stays valid for `delay` ms.
        if (!charged) {
            charged = timer.elapsedOnce(Math.max(0, (long) delay.get()));
            return;
        }
        if (timer.elapsedOnce(Math.max(60, (long) delay.get()))) {
            attack(mc, target);
        }
    }

    private boolean readyToAttack(MinecraftClient mc, PlayerEntity target) {
        boolean onCooldown = mc.player.getAttackCooldownProgress(0f) >= 1f;
        switch (mode.get()) {
            case "Normal":
                return !respectCooldown.get() || onCooldown;
            case "Only Crits":
                return mc.player.fallDistance > 0 && !mc.player.isOnGround() && !mc.player.isTouchingWater()
                        && !mc.player.hasVehicle()
                        && (!respectCooldown.get() || onCooldown);
            case "Smart":
                boolean critReady = mc.player.fallDistance > 0 || mc.player.isTouchingWater();
                boolean safe = mc.player.getHealth() > 6f || critReady;
                boolean grounded = mc.player.isOnGround() && mc.player.fallDistance == 0;
                return (!respectCooldown.get() || onCooldown)
                        && (critReady || grounded) && safe;
            default:
                return onCooldown;
        }
    }

    private void attack(MinecraftClient mc, PlayerEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.resetLastAttackedTicks();
    }
}
