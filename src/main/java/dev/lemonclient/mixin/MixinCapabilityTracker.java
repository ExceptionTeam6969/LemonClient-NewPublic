package dev.lemonclient.mixin;

import dev.lemonclient.mixininterface.ICapabilityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.mojang.blaze3d.platform.GlStateManager$CapabilityTracker")
public abstract class MixinCapabilityTracker implements ICapabilityTracker {
    @Shadow
    private boolean state;

    @Shadow
    public abstract void setState(boolean state);

    @Override
    public boolean get() {
        return state;
    }

    @Override
    public void set(boolean state) {
        setState(state);
    }
}
