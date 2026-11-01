package ru.turik.mixin;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Mutable access to the explosion knockback vector for Velocity. */
@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {

    @Accessor("playerKnockback")
    net.minecraft.util.math.Vec3d turik$getPlayerKnockback();

    @Accessor("playerKnockback")
    @Mutable
    @Final
    void turik$setPlayerKnockback(net.minecraft.util.math.Vec3d value);
}
