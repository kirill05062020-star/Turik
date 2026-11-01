package ru.turik.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import ru.turik.Turik;
import ru.turik.mixin.EntityVelocityUpdateS2CPacketAccessor;
import ru.turik.mixin.ExplosionS2CPacketAccessor;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 3. Velocity — scales knockback from packets before the game applies it.
 * Separate horizontal/vertical for hit knockback and explosions.
 */
public class Velocity extends Module {
    private final NumberSetting horizontal = new NumberSetting("Horizontal", 0, 100, 1, 0, "%");
    private final NumberSetting vertical = new NumberSetting("Vertical", 0, 100, 1, 0, "%");
    private final NumberSetting explosionH = new NumberSetting("Explosion horizontal", 0, 100, 1, 0, "%");
    private final NumberSetting explosionV = new NumberSetting("Explosion vertical", 0, 100, 1, 0, "%");
    private final BooleanSetting onlyWhenEnabled = new BooleanSetting("Apply only when enabled", true);

    public Velocity() {
        super("Velocity", "Настройка реакции на отбрасывание",
                Category.COMBAT);
        add(horizontal, vertical, explosionH, explosionV, onlyWhenEnabled);
    }

    public void modifyEntityVelocity(EntityVelocityUpdateS2CPacket packet) {
        if (!isEnabled() && onlyWhenEnabled.get()) {
            return;
        }
        MinecraftClient mc = Turik.get().mc();
        if (mc.player == null || packet.turik$getTargetId() != mc.player.getId()) {
            return;
        }
        EntityVelocityUpdateS2CPacketAccessor acc = (EntityVelocityUpdateS2CPacketAccessor) (Object) packet;
        acc.turik$setVelocityX(scale(acc.turik$getVelocityX(), horizontal.get()));
        acc.turik$setVelocityY(scale(acc.turik$getVelocityY(), vertical.get()));
        acc.turik$setVelocityZ(scale(acc.turik$getVelocityZ(), horizontal.get()));
    }

    public void modifyExplosion(ExplosionS2CPacket packet) {
        if (!isEnabled() && onlyWhenEnabled.get()) {
            return;
        }
        ExplosionS2CPacketAccessor acc = (ExplosionS2CPacketAccessor) (Object) packet;
        net.minecraft.util.math.Vec3d kb = acc.turik$getPlayerKnockback();
        acc.turik$setPlayerKnockback(new net.minecraft.util.math.Vec3d(
                kb.x * explosionH.get() / 100.0,
                kb.y * explosionV.get() / 100.0,
                kb.z * explosionH.get() / 100.0));
    }

    private static int scale(int raw, double percent) {
        return (int) Math.round(raw * percent / 100.0);
    }
}
