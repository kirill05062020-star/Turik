package ru.turik.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.turik.Turik;
import ru.turik.modules.movement.NoSlow;

/**
 * NoSlow: removes the using-item slowdown by reporting "not using" to the
 * movement code while the module is active and the item is selected.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void turik$noSlow(CallbackInfoReturnable<Boolean> cir) {
        if (Turik.get() == null) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.getWorld().isClient)) return;

        NoSlow noSlow = Turik.get().modules().get(NoSlow.class);
        if (noSlow != null && noSlow.shouldHideUsing(self)) {
            cir.setReturnValue(false);
        }
    }
}
