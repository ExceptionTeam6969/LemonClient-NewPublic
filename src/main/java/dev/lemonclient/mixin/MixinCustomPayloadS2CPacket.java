package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.AntiPacketKick;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CustomPayloadS2CPacket.class)
public class MixinCustomPayloadS2CPacket {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 1048576))
    private int maxValue(int value) {
        return Modules.get().isActive(AntiPacketKick.class) ? Integer.MAX_VALUE : value;
    }
}
