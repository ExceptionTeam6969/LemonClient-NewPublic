package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Ambience;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {
    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private static void onGetWaterColor(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = Modules.get().get(Ambience.class);

        if (ambience.isActive() && ambience.customWaterColor.get()) {
            info.setReturnValue(ambience.waterColor.get().getPacked());
        }
    }

    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColor(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = Modules.get().get(Ambience.class);

        if (ambience.isActive() && ambience.customFoliageColor.get()) {
            info.setReturnValue(ambience.foliageColor.get().getPacked());
        }
    }

    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private static void onGetGrassColor(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        Ambience ambience = Modules.get().get(Ambience.class);

        if (ambience.isActive() && ambience.customGrassColor.get()) {
            info.setReturnValue(ambience.grassColor.get().getPacked());
        }
    }
}
