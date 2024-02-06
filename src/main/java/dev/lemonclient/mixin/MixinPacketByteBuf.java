package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.AntiPacketKick;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {
    @ModifyArg(method = "readNbt()Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt(Lnet/minecraft/nbt/NbtTagSizeTracker;)Lnet/minecraft/nbt/NbtCompound;"))
    private NbtTagSizeTracker xlPackets(NbtTagSizeTracker sizeTracker) {
        return Modules.get().isActive(AntiPacketKick.class) ? NbtTagSizeTracker.EMPTY : sizeTracker;
    }
}
