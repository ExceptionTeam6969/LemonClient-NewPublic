package dev.lemonclient.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMain {
    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void beforeStart(CallbackInfo ci) {
        System.setProperty("java.awt.headless", "false");
    }
}
