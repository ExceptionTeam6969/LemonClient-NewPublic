package dev.lemonclient.render.animation;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.systems.modules.chat.AutoEsu;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import static dev.lemonclient.LemonClient.mc;

public class CaptureMark {
    private static float espValue = 1f, prevEspValue;
    private static float espSpeed = 1f;
    private static boolean flipSpeed;

    public static void render(Entity target, AutoEsu esu) {
        Camera camera = mc.gameRenderer.getCamera();

        double tPosX = MathUtils.interpolate(target.prevX, target.getX(), mc.getTickDelta()) - camera.getPos().x;
        double tPosY = MathUtils.interpolate(target.prevY, target.getY(), mc.getTickDelta()) - camera.getPos().y;
        double tPosZ = MathUtils.interpolate(target.prevZ, target.getZ(), mc.getTickDelta()) - camera.getPos().z;

        MatrixStack matrices = new MatrixStack();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(tPosX, (tPosY + target.getEyeHeight(target.getPose()) / 2f), tPosZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathUtils.interpolateFloat(prevEspValue, espValue, mc.getTickDelta())));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        RenderSystem.setShaderTexture(0, new Identifier(LemonClient.MOD_ID, "textures/capture.png"));

        matrices.translate(-0.75, -0.75, -0.01);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(() -> Render2DUtils.TEXTURE_COLOR_PROGRAM.backingProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        bufferBuilder.vertex(matrix, 0, 1.5f, 0).texture(0f, 1f).color(esu.getColor(90).getPacked()).next();
        bufferBuilder.vertex(matrix, 1.5f, 1.5f, 0).texture(1f, 1f).color(esu.getColor(0).getPacked()).next();
        bufferBuilder.vertex(matrix, 1.5f, 0, 0).texture(1f, 0).color(esu.getColor(180).getPacked()).next();
        bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).color(esu.getColor(270).getPacked()).next();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        immediate.draw();

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    public static void tick() {
        prevEspValue = espValue;
        espValue += espSpeed;
        if (espSpeed > 25) flipSpeed = true;
        if (espSpeed < -25) flipSpeed = false;
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
    }
}
