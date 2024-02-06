package dev.lemonclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.entity.effect.StatusEffectInstance.class)
public interface IStatusEffectInstance {
    @Accessor("duration")
    void setDuration(int duration);

    @Accessor("amplifier")
    void setAmplifier(int amplifier);
}
