package dev.lemonclient.mixin;

import dev.lemonclient.mixininterface.IBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class MixinBakedQuad implements IBakedQuad {
    @Shadow
    @Final
    protected int[] vertexData;

    @Override
    public float lemonclient$getX(int vertexI) {
        return Float.intBitsToFloat(vertexData[vertexI * 8]);
    }

    @Override
    public float lemonclient$getY(int vertexI) {
        return Float.intBitsToFloat(vertexData[vertexI * 8 + 1]);
    }

    @Override
    public float lemonclient$getZ(int vertexI) {
        return Float.intBitsToFloat(vertexData[vertexI * 8 + 2]);
    }
}
