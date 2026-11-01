package ru.turik.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.turik.Turik;
import ru.turik.event.events.Events;

/** Posts raw mouse button events when no screen is open (MiddleClickFriend). */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void turik$mouse(long window, int button, int action, int mods, CallbackInfo ci) {
        if (Turik.get() == null) return;
        if (Turik.get().mc().currentScreen == null) {
            Turik.get().events().post(new Events.MouseButtonEvent(button, action, mods));
        }
    }
}
