package ru.turik.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.turik.Turik;
import ru.turik.modules.movement.NoFall;
import ru.turik.modules.player.AutoRespawn;

/**
 * Client-player hooks: NoFall packet mode and AutoRespawn.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void turik$onTick(CallbackInfo ci) {
        if (Turik.get() == null) return;
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;

        NoFall noFall = Turik.get().modules().get(NoFall.class);
        if (noFall != null) {
            noFall.onPlayerTick(self);
        }

        AutoRespawn respawn = Turik.get().modules().get(AutoRespawn.class);
        if (respawn != null) {
            respawn.onPlayerTick(self);
        }
    }

    /** Sends an on-ground packet so the server never computes fall damage. */
    private void turik$sendOnGround(boolean onGround) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        self.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, false));
    }
}
