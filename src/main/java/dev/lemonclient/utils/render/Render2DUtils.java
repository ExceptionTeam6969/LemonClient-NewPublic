package dev.lemonclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.shaders.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Render2DUtils {
    public static TextureColorProgram TEXTURE_COLOR_PROGRAM;
    public static RoundedGradientProgram ROUNDED_GRADIENT_PROGRAM;
    public static RoundedProgram ROUNDED_PROGRAM;
    public static RectProgram RECT_PROGRAM;
    public static GradientGlowProgram GRADIENT_GLOW_PROGRAM;

    public static void initShaders() {
        if (ROUNDED_GRADIENT_PROGRAM == null)
            ROUNDED_GRADIENT_PROGRAM = new RoundedGradientProgram();
        if (ROUNDED_PROGRAM == null)
            ROUNDED_PROGRAM = new RoundedProgram();
        if (RECT_PROGRAM == null)
            RECT_PROGRAM = new RectProgram();
        if (GRADIENT_GLOW_PROGRAM == null)
            GRADIENT_GLOW_PROGRAM = new GradientGlowProgram();
        if (TEXTURE_COLOR_PROGRAM == null)
            TEXTURE_COLOR_PROGRAM = new TextureColorProgram();
    }

    public static void drawRoundShader(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        preShaderDraw(matrices, x, y, width, height);
        ROUNDED_PROGRAM.setParameters(x, y, width, height, radius, color);
        ROUNDED_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawRectShader(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        preShaderDraw(matrices, x, y, width, height);
        RECT_PROGRAM.setParameters(color);
        RECT_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawGradientRoundShader(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius) {
        preShaderDraw(matrices, x, y, width, height);
        ROUNDED_GRADIENT_PROGRAM.setParameters(x, y, width, height, radius, color1, color2, color3, color4);
        ROUNDED_GRADIENT_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawGradientGlow(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius, float softness) {
        preShaderDraw(matrices, x - 10, y - 10, width + 20, height + 20);
        GRADIENT_GLOW_PROGRAM.setParameters(x, y, width, height, radius, softness, color1, color2, color3, color4);
        GRADIENT_GLOW_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
    }

    public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
        buffer.vertex(matrix, x, y, 0).next();
        buffer.vertex(matrix, x, y1, 0).next();
        buffer.vertex(matrix, x1, y1, 0).next();
        buffer.vertex(matrix, x1, y, 0).next();
    }

    public static void drawTexture(DrawContext context, Identifier icon, int x, int y, int width, int height) {
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.enableBlend();
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        context.drawTexture(icon, x, y, 0, 0, width, height, width, height);
    }

    public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u + 0.0F) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u + 0.0F) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawRectDumbWay(MatrixStack matrices, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y1, 0.0F).color(c1.getPacked()).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(c2.getPacked()).next();
        bufferBuilder.vertex(matrix, x1, y, 0.0F).color(c3.getPacked()).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c4.getPacked()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.r, color.g, color.b, MathHelper.clamp(alpha, 0, 255));
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((System.currentTimeMillis() + delay) / 16f);
        rainbow %= 360;
        return new Color(java.awt.Color.HSBtoRGB((float) (rainbow / 360), saturation, brightness));
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(java.awt.Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.r, color.g, color.b, Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return new Color(java.awt.Color.getHSBColor((double) ((float) ((angle %= 360) / 360.0)) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0), 0.5F, 1.0F));
    }

    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = java.awt.Color.RGBtoHSB(color.r, color.g, color.b, null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.r, colorHSB.g, colorHSB.b, Math.max(0, Math.min(255, (int) (alpha * 255))));
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(MathUtils.interpolateInt(color1.r, color2.r, amount), MathUtils.interpolateInt(color1.g, color2.g, amount), MathUtils.interpolateInt(color1.b, color2.b, amount), MathUtils.interpolateInt(color1.a, color2.a, amount));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
        int angle = (int) (((System.currentTimeMillis()) / speed + count) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColorC(cl1, cl2, angle / 360f);
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = java.awt.Color.RGBtoHSB(color1.r, color1.g, color1.b, null);
        float[] color2HSB = java.awt.Color.RGBtoHSB(color2.r, color2.g, color2.b, null);

        Color resultColor = new Color(java.awt.Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount), MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount)));

        return new Color(resultColor.r, resultColor.g, resultColor.b, MathUtils.interpolateInt(color1.a, color2.a, amount));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f) : interpolateColorC(start, end, angle / 360f);
    }

    public static Color getAnalogousColor(Color color) {
        float[] hsb = java.awt.Color.RGBtoHSB(color.r, color.g, color.b, null);
        float degree = 0.84f;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(java.awt.Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }
}
