package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBorderDebugRenderer.class)
public class MixinChunkBorderDebugRenderer {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getChunkPos()Lnet/minecraft/util/math/ChunkPos;"))
    private ChunkPos render$getChunkPos(ChunkPos chunkPos) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (!freecam.isActive()) return chunkPos;

        float delta = client.getTickDelta();

        return new ChunkPos(
            ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getX(delta))),
            ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getZ(delta)))
        );
    }
}
