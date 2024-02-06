package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.movement.Jesus;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static dev.lemonclient.LemonClient.mc;

@Mixin(PowderSnowBlock.class)
public class MixinPowderSnowBlock {
    @ModifyReturnValue(method = "canWalkOnPowderSnow", at = @At("RETURN"))
    private static boolean onCanWalkOnPowderSnow(boolean original, Entity entity) {
        if (entity == mc.player && Modules.get().get(Jesus.class).canWalkOnPowderSnow()) {
            return true;
        }
        return original;
    }
}
