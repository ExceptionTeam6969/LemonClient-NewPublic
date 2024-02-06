package dev.lemonclient.mixin.sodium;

import dev.lemonclient.systems.modules.render.Xray;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public class MixinSodiumBlockRenderer {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void onRenderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers, CallbackInfo info) {
        int alpha = Xray.getAlpha(ctx.state(), ctx.pos());

        if (alpha == 0) info.cancel();
    }
}
