package ru.turik.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the vanilla item use cooldown so FastUse can shorten it. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("itemUseCooldown")
    int turik$getItemUseCooldown();

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int value);
}
