package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Ambience;
import net.minecraft.client.color.world.FoliageColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoliageColors.class)
public class MixinFoliageColors {

    @ModifyReturnValue(method = "getBirchColor", at = @At("RETURN"))
    private static int onGetBirchColor(int original) {
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFoliageColor.get()) {
            return ambience.foliageColor.get().getPacked();
        }

        return original;
    }

    @ModifyReturnValue(method = "getSpruceColor", at = @At("RETURN"))
    private static int onGetSpruceColor(int original) {
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFoliageColor.get()) {
            return ambience.foliageColor.get().getPacked();
        }

        return original;
    }
}
