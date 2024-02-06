package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.systems.config.Config;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(SplashTextResourceSupplier.class)
public class MixinSplashTextResourceSupplier {
    private boolean override = true;
    private final Random random = new Random();

    private final List<String> splashes = Arrays.asList(
        "Powered by LemonClient Development!",
        "Try .irc chat!",
        "pvp.obsserver.cn",
        "§6Fin_LemonKee §fbased god",
        "Enjoy your gaming time!",
        LemonClient.NAME + " " + LemonClient.VERSION + "!"
    );

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashTextRenderer> cir) {
        if (Config.get() == null || !Config.get().titleScreenSplashes.get()) return;

        if (override) cir.setReturnValue(new SplashTextRenderer(splashes.get(random.nextInt(splashes.size()))));
        override = !override;
    }
}
