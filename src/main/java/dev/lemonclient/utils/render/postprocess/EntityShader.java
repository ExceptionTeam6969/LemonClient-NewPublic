package dev.lemonclient.utils.render.postprocess;

import dev.lemonclient.mixin.IWorldRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;

import static dev.lemonclient.LemonClient.mc;

public abstract class EntityShader extends PostProcessShader {
    private Framebuffer prevBuffer;

    @Override
    protected void preDraw() {
        WorldRenderer worldRenderer = mc.worldRenderer;
        IWorldRenderer wra = (IWorldRenderer) worldRenderer;
        prevBuffer = worldRenderer.getEntityOutlinesFramebuffer();
        wra.setEntityOutlinesFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        if (prevBuffer == null) return;

        WorldRenderer worldRenderer = mc.worldRenderer;
        IWorldRenderer wra = (IWorldRenderer) worldRenderer;
        wra.setEntityOutlinesFramebuffer(prevBuffer);
        prevBuffer = null;
    }

    public void endRender() {
        endRender(() -> vertexConsumerProvider.draw());
    }
}
