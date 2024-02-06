package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.NoRender;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkyLightProvider {
    @Inject(at = @At("HEAD"), method = "method_51531", cancellable = true)
    private void recalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noSkylightUpdates()) ci.cancel();
    }
}
