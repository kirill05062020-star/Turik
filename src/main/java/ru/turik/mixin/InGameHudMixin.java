package ru.turik.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.turik.Turik;
import ru.turik.modules.visuals.NoRender;

/** NoRender: hides the vanilla fire overlay (require=0: may be refactored upstream). */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderFireOverlay*", at = @At("HEAD"), cancellable = true, require = 0)
    private void turik$noFire(CallbackInfo ci) {
        if (Turik.get() == null) return;
        NoRender noRender = Turik.get().modules().get(NoRender.class);
        if (noRender != null && noRender.hideFire()) {
            ci.cancel();
        }
    }
}
