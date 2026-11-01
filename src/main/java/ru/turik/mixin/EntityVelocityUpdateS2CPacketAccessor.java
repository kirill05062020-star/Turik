package ru.turik.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Final;

/** Mutable access to velocity packet fields for the Velocity module. */
@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {

    @Accessor("id")
    int turik$getTargetId();

    @Accessor("velocityX")
    @Mutable
    @Final
    void turik$setVelocityX(int value);

    @Accessor("velocityY")
    @Mutable
    @Final
    void turik$setVelocityY(int value);

    @Accessor("velocityZ")
    @Mutable
    @Final
    void turik$setVelocityZ(int value);
}
