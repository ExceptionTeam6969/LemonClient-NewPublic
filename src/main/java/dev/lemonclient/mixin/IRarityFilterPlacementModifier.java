package dev.lemonclient.mixin;

import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RarityFilterPlacementModifier.class)
public interface IRarityFilterPlacementModifier {
    @Accessor
    int getChance();
}
