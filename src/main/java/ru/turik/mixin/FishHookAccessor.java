package ru.turik.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** AutoFish reads the bobber's private "caught fish" latch. */
@Mixin(FishingBobberEntity.class)
public interface FishHookAccessor {

    @Accessor("caughtFish")
    boolean turik$caughtFish();
}
