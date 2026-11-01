package ru.turik.mixin;

import net.minecraft.client.world.ClientWorld;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.turik.Turik;
import ru.turik.modules.world.SkyOverride;

/**
 * Ambience / Atmosphere / TimeChanger hook points: sky color override and
 * day-cycle time override.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"))
    private void turik$skyColor(net.minecraft.util.math.Vec3d cameraPos, float tickDelta,
                                CallbackInfoReturnable<Vector3f> cir) {
        if (Turik.get() == null) return;
        SkyOverride override = Turik.get().modules().skyOverride();
        if (override != null) {
            float[] rgb = override.skyColor();
            if (rgb != null) {
                cir.setReturnValue(new Vector3f(rgb[0], rgb[1], rgb[2]));
            }
        }
    }

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void turik$time(CallbackInfoReturnable<Float> cir) {
        if (Turik.get() == null) return;
        ru.turik.modules.world.TimeChanger timeChanger = Turik.get().modules().get(ru.turik.modules.world.TimeChanger.class);
        if (timeChanger != null && timeChanger.isEnabled()) {
            Float override = timeChanger.timeOverride();
            if (override != null) {
                cir.setReturnValue(override);
            }
        }
    }
}
