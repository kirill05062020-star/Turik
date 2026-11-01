package ru.turik.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.turik.Turik;
import ru.turik.modules.combat.Velocity;

/** Velocity: scales knockback packets before the game applies them. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"))
    private void turik$velocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (Turik.get() == null) return;
        Velocity module = Turik.get().modules().get(Velocity.class);
        if (module != null) {
            module.modifyEntityVelocity(packet);
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"))
    private void turik$explosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (Turik.get() == null) return;
        Velocity module = Turik.get().modules().get(Velocity.class);
        if (module != null) {
            module.modifyExplosion(packet);
        }
    }
}
