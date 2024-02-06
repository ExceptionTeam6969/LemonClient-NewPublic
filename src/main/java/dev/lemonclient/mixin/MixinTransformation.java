package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.ApplyTransformationEvent;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Transformation.class)
public class MixinTransformation {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onApply(boolean leftHanded, MatrixStack matrices, CallbackInfo info) {
        ApplyTransformationEvent event = LemonClient.EVENT_BUS.post(ApplyTransformationEvent.get((Transformation) (Object) this, leftHanded, matrices));
        if (event.isCancelled()) info.cancel();
    }
}
