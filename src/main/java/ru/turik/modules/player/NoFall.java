package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 19. NoFall — prevents fall damage.
 * Packet: reports on-ground to the server right before landing.
 * Damping: bleeds off vertical speed near the ground (legit-style).
 */
public class NoFall extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Damping");
    private final NumberSetting threshold = new NumberSetting("Fall threshold", 0.5, 10, 0.5, 2.5, "блоков");
    private final BooleanSetting onlyWhenAlive = new BooleanSetting("Skip in water/vehicle", true);

    public NoFall() {
        super("NoFall", "Устраняет урон от падения",
                Category.PLAYER);
        add(mode, threshold, onlyWhenAlive);
    }

    /** Called from the client-player mixin. */
    public void onPlayerTick(ClientPlayerEntity player) {
        if (!isEnabled() || player.getWorld() == null || Turik.get().mc().world == null) {
            return;
        }
        if (player.isTouchingWater() || player.hasVehicle() || player.isDead()) {
            return;
        }
        if (player.fallDistance < threshold.get()) {
            return;
        }
        if (mode.is("Packet")) {
            if (nearGround(player)) {
                player.fallDistance = 0;
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
            }
        } else {
            // Damping: clamp the incoming fall speed close to the ground.
            if (nearGround(player) && player.getVelocity().y < -0.6) {
                player.setVelocity(player.getVelocity().x, -0.35, player.getVelocity().z);
            }
        }
    }

    private boolean nearGround(ClientPlayerEntity player) {
        MinecraftClient mc = Turik.get().mc();
        var pos = player.getBlockPos();
        for (int dy = 0; dy <= 2; dy++) {
            var state = mc.world.getBlockState(pos.down(dy));
            if (!state.isAir() && state.isSolidBlock(mc.world, pos.down(dy))) {
                return true;
            }
        }
        return false;
    }
}
