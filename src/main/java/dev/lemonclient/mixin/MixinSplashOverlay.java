package dev.lemonclient.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static dev.lemonclient.LemonClient.mc;

@Mixin(SplashOverlay.class)
public abstract class MixinSplashOverlay {
    @Final
    @Shadow
    private boolean reloading;

    @Shadow
    private float progress;

    @Shadow
    private long reloadCompleteTime = -1L;

    @Shadow
    private long reloadStartTime = -1L;

    @Final
    @Shadow
    private ResourceReload reload;

    @Final
    @Shadow
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Shadow
    protected abstract void renderProgressBar(DrawContext drawContext, int minX, int minY, int maxX, int maxY, float opacity);

    @Unique
    private static final Identifier CLIENT_LOGO = new Identifier("lemon-client", "textures/loadscreen.png");

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.get().customSplashOverlay.get()) return;

        ci.cancel();

        int i = mc.getWindow().getScaledWidth();
        int j = mc.getWindow().getScaledHeight();
        long l = Util.getMeasuringTimeMs();
        if (reloading && reloadStartTime == -1L) {
            reloadStartTime = l;
        }

        float f = reloadCompleteTime > -1L ? (float) (l - reloadCompleteTime) / 1000.0F : -1.0F;
        float g = reloadStartTime > -1L ? (float) (l - reloadStartTime) / 500.0F : -1.0F;
        float h;
        int k;
        if (f >= 1.0F) {
            if (mc.currentScreen != null) {
                mc.currentScreen.render(context, 0, 0, delta);
            }

            k = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            context.fill(0, 0, i, j, withAlpha(new Color(255, 185, 0).getPacked(), k));
            h = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (reloading) {
            if (mc.currentScreen != null && g < 1.0F) {
                mc.currentScreen.render(context, mouseX, mouseY, delta);
            }

            k = MathHelper.ceil(MathHelper.clamp(g, 0.15, 1.0) * 255.0);
            context.fill(0, 0, i, j, withAlpha(new Color(255, 185, 0).getPacked(), k));
            h = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            k = new Color(255, 185, 0).getPacked();
            float m = (float) (k >> 16 & 255) / 255.0F;
            float n = (float) (k >> 8 & 255) / 255.0F;
            float o = (float) (k & 255) / 255.0F;
            GlStateManager._clearColor(m, n, o, 1.0F);
            GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
            h = 1.0F;
        }

        k = (int) ((double) context.getScaledWindowWidth() * 0.5);
        int p = (int) ((double) context.getScaledWindowHeight() * 0.5);
        double d = Math.min((double) context.getScaledWindowWidth() * 0.75, context.getScaledWindowHeight()) * 0.25;
        double e = d * 4.0;
        int r = (int) (e * 0.5);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);

        Render2DUtils.drawTexture(context, CLIENT_LOGO, k - 64, p - 64, 128, 128);

        int s = (int) ((double) context.getScaledWindowHeight() * 0.8325);
        float t = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
        if (f < 1.0F) {
            renderProgressBar(context, i / 2 - r, s - 5, i / 2 + r, s + 5, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F));
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        if (f >= 2.0F) {
            mc.setOverlay(null);
        }

        if (reloadCompleteTime == -1L && reload.isComplete() && (!reloading || g >= 2.0F)) {
            try {
                reload.throwException();
                exceptionHandler.accept(Optional.empty());
            } catch (Throwable var23) {
                exceptionHandler.accept(Optional.of(var23));
            }

            reloadCompleteTime = Util.getMeasuringTimeMs();
            if (mc.currentScreen != null) {
                mc.currentScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            }

            Managers.SOUND.clientStartupSound.play(1.0f);
        }
    }

    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }
}
