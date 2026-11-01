package ru.turik.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets the Weather and NoRender modules drive the rain gradient client-side. */
@Mixin(World.class)
public interface WorldInvoker {

    @Invoker("setRainGradient")
    void turik$setRainGradient(float intensity);
}
