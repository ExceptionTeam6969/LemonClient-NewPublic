package dev.lemonclient.managers.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.mixininterface.IShaderEffect;
import dev.lemonclient.renderer.shader.PostShader;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Shaders;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.lemonclient.LemonClient.mc;

public class ShaderManager {
    private final static List<RenderTask> tasks = new ArrayList<>();
    private ThunderHackFramebuffer shaderBuffer;

    public float time = 0;

    // Post shaders
    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;

    public static PostShader MOTION_BLUR;

    private float currentBlur;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        if (DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
        }

        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void renderShader(PostShader shader, Consumer<PostShader> task) {
        shader.render(mc.getTickDelta());
    }

    public void applyShader(Runnable runnable, Shader mode) {
        Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (shaderBuffer.textureWidth != MCBuffer.textureWidth || shaderBuffer.textureHeight != MCBuffer.textureHeight)
            shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, shaderBuffer.fbo);
        shaderBuffer.beginWrite(true);
        runnable.run();
        shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, MCBuffer.fbo);
        MCBuffer.beginWrite(false);
        ManagedShaderEffect shader = getShader(mode);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = shader.getShaderEffect();

        if (effect != null)
            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufIn", shaderBuffer);

        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        setupShader(mode, shader);
        shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        Shaders shaders = Modules.get().get(Shaders.class);
        if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha0", shaders.glow.get() ? -1.0f : shaders.outlineColor.get().a / 255.0f);
            effect.setUniformValue("alpha1", shaders.fillAlpha.get() / 255f);
            effect.setUniformValue("alpha2", shaders.alpha2.get() / 255f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.get());
            effect.setUniformValue("oct", shaders.octaves.get());
            effect.setUniformValue("quality", shaders.quality.get());
            effect.setUniformValue("factor", shaders.factor.get().floatValue());
            effect.setUniformValue("moreGradient", shaders.gradient.get().floatValue());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha0", shaders.glow.get() ? -1.0f : shaders.outlineColor.get().a / 255.0f);
            effect.setUniformValue("alpha1", shaders.fillAlpha.get() / 255f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.get());
            effect.setUniformValue("quality", shaders.quality.get());
            effect.setUniformValue("first", shaders.outlineColor.get().r / 255f, shaders.outlineColor.get().g / 255f, shaders.outlineColor.get().b / 255f, shaders.outlineColor.get().a / 255f);
            effect.setUniformValue("second", shaders.outlineColor1.get().r / 255f, shaders.outlineColor1.get().g / 255f, shaders.outlineColor1.get().b / 255f);
            effect.setUniformValue("third", shaders.outlineColor2.get().r / 255f, shaders.outlineColor2.get().g / 255f, shaders.outlineColor2.get().b / 255f);
            effect.setUniformValue("ffirst", shaders.fillColor1.get().r / 255f, shaders.fillColor1.get().g / 255f, shaders.fillColor1.get().b / 255f, shaders.fillColor1.get().a / 255f);
            effect.setUniformValue("fsecond", shaders.fillColor2.get().r / 255f, shaders.fillColor2.get().g / 255f, shaders.fillColor2.get().b / 255f);
            effect.setUniformValue("fthird", shaders.fillColor3.get().r / 255f, shaders.fillColor3.get().g / 255f, shaders.fillColor3.get().b / 255f);
            effect.setUniformValue("oct", shaders.octaves.get());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += 0.008f;
        } else if (shader == Shader.Default) {
            effect.setUniformValue("alpha0", shaders.glow.get() ? -1.0f : shaders.outlineColor.get().a / 255.0f);
            effect.setUniformValue("lineWidth", shaders.lineWidth.get());
            effect.setUniformValue("quality", shaders.quality.get());
            effect.setUniformValue("color", shaders.fillColor1.get().r / 255f, shaders.fillColor1.get().g / 255f, shaders.fillColor1.get().b / 255f, shaders.fillColor1.get().a / 255f);
            effect.setUniformValue("outlinecolor", shaders.outlineColor.get().r / 255f, shaders.outlineColor.get().g / 255f, shaders.outlineColor.get().b / 255f, shaders.outlineColor.get().a / 255f);
            effect.render(mc.getTickDelta());
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", shaders.fillColor1.get().r / 255f, shaders.fillColor1.get().g / 255f, shaders.fillColor1.get().b / 255f, shaders.fillColor1.get().a / 255f);
            effect.setUniformValue("quality", shaders.quality.get());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += 0.008f;
        }
    }


    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).lemonclient$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        MOTION_BLUR = new PostShader(new Identifier("minecraft", "shaders/post/motion_blur.json"), null);
    }

    public void renderMotionBlur(float blurAmount) {
        MOTION_BLUR.set("BlendFactor", blurAmount);
        MOTION_BLUR.render(mc.getTickDelta());
    }

    public static class ThunderHackFramebuffer extends Framebuffer {
        public ThunderHackFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            resize(width, height, true);
            setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
            return true;
        }

        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default,
        Smoke,
        Gradient,
        Snow
    }
}
