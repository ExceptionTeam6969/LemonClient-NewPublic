package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.NoRender;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class MixinToastManager {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void onAddToast(Toast toast, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noToasts()) ci.cancel();
    }
}
