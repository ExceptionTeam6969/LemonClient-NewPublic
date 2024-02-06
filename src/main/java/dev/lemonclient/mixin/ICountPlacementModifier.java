package dev.lemonclient.mixin;

import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CountPlacementModifier.class)
public interface ICountPlacementModifier {
    @Accessor
    IntProvider getCount();
}
