package ru.turik.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.turik.Turik;
import ru.turik.event.events.Events;
import ru.turik.modules.combat.NoFriendDamage;
import ru.turik.modules.player.FastBreak;

/**
 * Interaction hooks: friend-safe attacks and FastBreak acceleration.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    public abstract boolean updateBlockBreakingProgress(BlockPos pos, Direction direction);

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void turik$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (Turik.get() == null) return;

        Events.AttackEvent event = new Events.AttackEvent(target);
        Turik.get().events().post(event);
        NoFriendDamage guard = Turik.get().modules().get(NoFriendDamage.class);
        if (event.isCancelled() || (guard != null && guard.blocked(target))) {
            ci.cancel();
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("TAIL"))
    private void turik$fastBreak(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (Turik.get() == null) return;
        FastBreak fastBreak = Turik.get().modules().get(FastBreak.class);
        if (fastBreak != null) {
            fastBreak.accelerate(this::updateBlockBreakingProgress, pos, direction);
        }
    }
}
