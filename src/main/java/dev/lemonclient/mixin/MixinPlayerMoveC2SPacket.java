package dev.lemonclient.mixin;

import dev.lemonclient.mixininterface.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerMoveC2SPacket.class)
public class MixinPlayerMoveC2SPacket implements IPlayerMoveC2SPacket {
    @Unique
    private int tag;

    @Override
    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public int getTag() {
        return this.tag;
    }
}
