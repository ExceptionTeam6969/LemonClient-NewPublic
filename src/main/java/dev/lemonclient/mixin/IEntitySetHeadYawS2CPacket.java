package dev.lemonclient.mixin;

import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySetHeadYawS2CPacket.class)
public interface IEntitySetHeadYawS2CPacket {
    @Accessor("entity")
    int getId();
}
