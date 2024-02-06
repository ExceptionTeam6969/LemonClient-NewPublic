package dev.lemonclient.mixin;

import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.InvRenderer;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.gui.MeteorRenderer;
import dev.lemonclient.utils.render.gui.NewParticleRenderer;
import dev.lemonclient.utils.render.gui.ParticleRenderer;
import dev.lemonclient.utils.render.gui.SnowRenderer;
import dev.lemonclient.utils.timers.MSTimer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.lemonclient.LemonClient.mc;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;renderBackground(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MSTimer timer = new MSTimer();
        SnowRenderer snowRenderer = new SnowRenderer();
        MeteorRenderer meteorSystem = new MeteorRenderer(30);
        NewParticleRenderer particleRenderer = new NewParticleRenderer();
        ParticleRenderer particleRenderer0 = new ParticleRenderer(300);

        InvRenderer invRenderer = Modules.get().get(InvRenderer.class);
        if (!invRenderer.isActive()) return;

        MSAAFramebuffer.use(() -> {
            if (invRenderer.lineMeteor.get()) {
                meteorSystem.setRainbow(invRenderer.meteorRainbow.get());
                meteorSystem.tick();
                meteorSystem.render(context);
            }

            if (invRenderer.snowParticles.get()) {
                snowRenderer.tick(invRenderer.spawnDelays.get(), context.getScaledWindowHeight());
                snowRenderer.render(context);
            }

            if (invRenderer.particles.get()) {
                if (invRenderer.bothParticles.get()) {
                    Renderer2D.COLOR.begin();
                    if (timer.hasTimePassed(50)) {
                        particleRenderer.update();
                        timer.reset();
                    }
                    particleRenderer.render(mouseX, mouseY);
                    Renderer2D.COLOR.render(context.getMatrices());
                }

                particleRenderer0.render(context, mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        });
    }
}
