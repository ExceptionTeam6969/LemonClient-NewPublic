package dev.lemonclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL32;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.utils.world.BlockInfo.getBox;
import static dev.lemonclient.utils.world.BlockInfo.getShape;


public class Render3DUtils {
    private static float prevCircleStep;
    private static float circleStep;

    public static boolean visibleHeight(RenderMode renderMode) {
        return renderMode == RenderMode.UpperSide || renderMode == RenderMode.LowerSide;
    }

    public static boolean visibleSide(ShapeMode shapeMode) {
        return shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Sides;
    }

    public static boolean visibleLine(ShapeMode shapeMode) {
        return shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Lines;
    }

    public static void render(RenderInfo ri, BlockPos blockPos, Color sideColor, Color lineColor, double height) {
        if (blockPos == null) return;

        switch (ri.renderMode) {
            case Box -> box(ri, blockPos, sideColor, lineColor);
            case UpperSide -> side(ri, blockPos, sideColor, lineColor, Side.Upper, height);
            case LowerSide -> side(ri, blockPos, sideColor, lineColor, Side.Lower, height);
            case Shape -> shape(ri, blockPos, sideColor, lineColor);
            case Romb -> romb(ri, blockPos, sideColor, lineColor, Side.Default, height);
            case UpperRomb -> romb(ri, blockPos, sideColor, lineColor, Side.Upper, height);
        }
    }

    private static void romb(RenderInfo ri, BlockPos blockPos, Color sideColor, Color lineColor, Side side, double height) {
        switch (side) {
            case Default -> {
                // North
                render(ri, blockPos, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0.0, 0.0, 0.5, 0.5, 0.0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 0.5, 0.0, 0.0, 0.5, 0.0, 0.5, 0.5, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0.5, 0.0, 0.0, 0.5, 0.0, 0.5, 0.5, 0, 0.5, 0, 0, 0.5, 0, 0, sideColor, lineColor, ri.shapeMode);

                // South
                render(ri, blockPos, 0.0, 0.0, 1.0, 0.0, 0.5, 0.0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0.0, 1.0, 0.5, 0.5, 0.0, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 0.5, 1.0, 0.0, 0.5, 0.0, 0.5, 0.5, 0, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0.5, 1.0, 0.0, 0.5, 0.0, 0.5, 0.5, 0, 0.5, 0, 0, 0.5, 0, 0, sideColor, lineColor, ri.shapeMode);

                // East
                render(ri, blockPos, 1.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 1.0, 0.5, 0.0, 0.0, 0.5, 0.0, 0.0, 0.5, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 1.0, 0.5, 0.5, 0.0, 0.5, 0.0, 0.0, 0.5, 0.5, 0, 0, 0.5, 0, 0.5, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 1.0, -0.5, 0.5, 0.0, 0.5, 0.0, 0.0, 1, 0.5, 0, 0.5, 0.5, 0, 0.5, 0, sideColor, lineColor, ri.shapeMode);

                // West
                render(ri, blockPos, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 0.5, 0.0, 0.0, 0.5, 0.0, 0.0, 0.5, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 0.5, 0.5, 0.0, 0.5, 0.0, 0.0, 0.5, 0.5, 0, 0, 0.5, 0, 0.5, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, -0.5, 0.5, 0.0, 0.5, 0.0, 0.0, 1, 0.5, 0, 0.5, 0.5, 0, 0.5, 0, sideColor, lineColor, ri.shapeMode);

                // Up
                render(ri, blockPos, 0.0, 1, 0.0, 0.5, 0.0, 0.0, 0.0, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 1, 0.0, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 1, 0.5, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0.5, 0.5, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 1, 0.5, 0.5, 0.0, 0.5, 0.0, 0, 0.5, 0, 0, 0.5, 0.0, 0, 0, sideColor, lineColor, ri.shapeMode);

                // Down
                render(ri, blockPos, 0.0, 0, 0.0, 0.5, 0.0, 0.0, 0.0, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0, 0.0, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 0, 0.5, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0.5, 0.5, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 0, 0.5, 0.5, 0.0, 0.5, 0.0, 0, 0.5, 0, 0, 0.5, 0.0, 0, 0, sideColor, lineColor, ri.shapeMode);
            }
            case Upper -> {
                // Up
                render(ri, blockPos, 0.0, 1, 0.0, 0.5, 0.0, 0.0, 0.0, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 1, 0.0, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0, 0, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.5, 1, 0.5, 0.5, 0.0, 0.0, 0.5, 0, 0.5, 0, 0, 0.5, 0.5, 0, 0, sideColor, lineColor, ri.shapeMode);
                render(ri, blockPos, 0.0, 1, 0.5, 0.5, 0.0, 0.5, 0.0, 0, 0.5, 0, 0, 0.5, 0.0, 0, 0, sideColor, lineColor, ri.shapeMode);
            }
        }
    }

    private static void render(RenderInfo ri, BlockPos blockPos, double x, double y, double z, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        Vec3d vec3d = new Vec3d(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);

        ri.event.renderer.side(vec3d.x + x1, vec3d.y + y1, vec3d.z + z1, vec3d.x + x2, vec3d.y + y2, vec3d.z + z2, vec3d.x + x3, vec3d.y + y3, vec3d.z + z3, vec3d.x + x4, vec3d.y + y4, vec3d.z + z4, sideColor, lineColor, shapeMode);
    }

    private static void line(RenderInfo ri, BlockPos blockPos, double x, double y, double z, double x1, double y1, double z1, Color lineColor) {
        Vec3d vec3d = new Vec3d(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);

        ri.event.renderer.line(vec3d.x + x, vec3d.y + y, vec3d.z + z, x1, y1, z1, lineColor);
    }

    private static void shape(RenderInfo ri, BlockPos blockPos, Color sideColor, Color lineColor) {
        if (getShape(blockPos).isEmpty()) return;

        render(ri, blockPos, getBox(blockPos), sideColor, lineColor);
    }

    private static void box(RenderInfo ri, BlockPos blockPos, Color sideColor, Color lineColor) {
        ri.event.renderer.box(blockPos, sideColor, lineColor, ri.shapeMode, 0);
    }

    private static void side(RenderInfo ri, BlockPos blockPos, Color sideColor, Color lineColor, Side side, double height) {
        double y = side == Side.Upper ? blockPos.getY() + 1 : blockPos.getY();
        ri.event.renderer.box(
            blockPos.getX(), blockPos.getY() + height, blockPos.getZ(),
            blockPos.getX() + 1, y, blockPos.getZ() + 1,
            sideColor, lineColor, ri.shapeMode, 0);
    }

    private static void render(RenderInfo ri, BlockPos blockPos, Box box, Color sideColor, Color lineColor) {
        ri.event.renderer.box(blockPos.getX() + box.minX, blockPos.getY() + box.minY, blockPos.getZ() + box.minZ, blockPos.getX() + box.maxX, blockPos.getY() + box.maxY, blockPos.getZ() + box.maxZ, sideColor, lineColor, ri.shapeMode, 0);
    }

    public static void thickRender(Render3DEvent event, BlockPos pos, ShapeMode mode, Color sideColor, Color sideColor2, Color lineColor, Color lineColor2, double lineSize) {
        double low = lineSize;
        double high = 1 - low;

        if (mode == ShapeMode.Lines || mode == ShapeMode.Both) {
            // Sides
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ() + low, lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + low, pos.getY() + 1, pos.getZ(), lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + low, lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ(), pos.getX() + high, pos.getY() + 1, pos.getZ(), lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ() + 1, pos.getX(), pos.getY() + 1, pos.getZ() + high, lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ() + 1, pos.getX() + low, pos.getY() + 1, pos.getZ() + 1, lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ() + 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + high, lineColor, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ() + 1, pos.getX() + high, pos.getY() + 1, pos.getZ() + 1, lineColor, lineColor2);

            // Up
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + high, pos.getZ(), lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getZ() + low, lineColor);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX(), pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + low, pos.getZ() + 1, lineColor);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ() + 1, pos.getX() + 1, pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ() + 1, pos.getX() + 1, pos.getZ() + high, lineColor);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX() + 1, pos.getY() + 1, pos.getZ(), pos.getX() + high, pos.getZ() + 1, lineColor);

            // Down
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + low, pos.getZ(), lineColor2, lineColor2);
            event.renderer.quadHorizontal(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getZ() + low, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + low, pos.getZ() + 1, lineColor2, lineColor2);
            event.renderer.quadHorizontal(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + low, pos.getZ() + 1, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ() + 1, pos.getX() + 1, pos.getY() + low, pos.getZ() + 1, lineColor2, lineColor2);
            event.renderer.quadHorizontal(pos.getX(), pos.getY(), pos.getZ() + 1, pos.getX() + 1, pos.getZ() + high, lineColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + low, pos.getZ() + 1, lineColor2, lineColor2);
            event.renderer.quadHorizontal(pos.getX() + 1, pos.getY(), pos.getZ(), pos.getX() + high, pos.getZ() + 1, lineColor2);
        }

        if (mode == ShapeMode.Sides || mode == ShapeMode.Both) {
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ(), sideColor, sideColor2);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ() + 1, sideColor, sideColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ() + 1, pos.getX() + 1, pos.getY() + 1, pos.getZ(), sideColor, sideColor2);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY(), pos.getZ() + 1, pos.getX(), pos.getY() + 1, pos.getZ() + 1, sideColor, sideColor2);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getZ() + 1, sideColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getZ() + 1, sideColor2);
        }
    }

    public static void thickUpperSide(Render3DEvent event, BlockPos pos, ShapeMode mode, Color sideColor, Color lineColor, double lineSize) {
        double low = lineSize;
        double high = 1 - low;

        if (mode == ShapeMode.Lines || mode == ShapeMode.Both) {
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + high, pos.getZ(), lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getZ() + low, lineColor);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX(), pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + low, pos.getZ() + 1, lineColor);
            event.renderer.gradientQuadVertical(pos.getX(), pos.getY() + 1, pos.getZ() + 1, pos.getX() + 1, pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ() + 1, pos.getX() + 1, pos.getZ() + high, lineColor);
            event.renderer.gradientQuadVertical(pos.getX() + 1, pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + high, pos.getZ() + 1, lineColor, lineColor);
            event.renderer.quadHorizontal(pos.getX() + 1, pos.getY() + 1, pos.getZ(), pos.getX() + high, pos.getZ() + 1, lineColor);
        }

        if (mode == ShapeMode.Sides || mode == ShapeMode.Both) {
            event.renderer.quadHorizontal(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getZ() + 1, sideColor);
        }
    }

    public enum Render {
        Meteor, LemonClient, None
    }

    public enum RenderMode {
        Box, Smooth, UpperSide, LowerSide, Shape, Romb, UpperRomb, None
    }

    public enum Side {
        Default, Upper, Lower
    }

    private static final VertexConsumerProvider.Immediate vertex = VertexConsumerProvider.immediate(new BufferBuilder(2048));

    public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color) {
        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        corner(x + w, y, radius, 360, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y, radius, 270, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y + h, radius, 180, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x + w, y + h, radius, 90, p, r, g, b, a, bufferBuilder, matrix4f);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        for (float i = angle; i > angle - 90; i -= 90 / p) {
            bufferBuilder.vertex(matrix4f, (float) (x + Math.cos(Math.toRadians(i)) * radius), (float) (y + Math.sin(Math.toRadians(i)) * radius), 0).color(r, g, b, a).next();
        }
    }

    public static void text(String text, MatrixStack stack, float x, float y, int color) {
        mc.textRenderer.draw(text, x, y, color, false, stack.peek().getPositionMatrix(), vertex, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        vertex.draw();
    }

    public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x + w, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y + h, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x + w, y + h, 0).color(r, g, b, a).next();


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void updateJello() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    public static void drawJello(MatrixStack matrix, Entity target, Color color) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getTickDelta();
        double prevSinAnim = absSinAnimation(cs - 0.45f);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * target.getHeight();

        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        float cos;
        float sin;
        for (int i = 0; i <= 30; i++) {
            cos = (float) (x + Math.cos(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            sin = (float) (z + Math.sin(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(color.getPacked()).next();
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(Render2DUtils.injectAlpha(color, 0).getPacked()).next();
        }

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }

    public static void drawGlowCircle(MatrixStack matrices, double posX, double posY, double posZ, float radius, ColorMode.ColorSettings settings) {
        drawGlowCircle(matrices, posX, posY, posZ, radius, settings.glowRadius.get().floatValue(), settings.inGlowRadius.get().floatValue(), settings);
    }

    public static void drawGlowCircle(MatrixStack matrices, double posX, double posY, double posZ, float radius, float glowRadius, float inGlowRadius, ColorMode.ColorSettings settings) {
        double x = posX - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = posY - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = posZ - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        matrices.push();
        matrices.translate(x, y, z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        float start = radius + glowRadius; // r + gR
        float middle = (start + radius) / 2; // (2r+gR)/2

        float k = Math.min(glowRadius, 1.0f) / Math.min(radius, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderCapUtils.enable("glow_circle", GL32.GL_LINE_SMOOTH);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        // Out circle
        /*

        __
        --

         */
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i += 5) {
            int clr = getColor(settings, i);
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * start, (float) 0, (float) v * start).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (150 * k)).getPacked()).next();
        }

        tessellator.draw();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        // In circle
        /*

            --
            __

         */

        for (int i = 0; i <= 360; i += 5) {
            int clr = getColor(settings, i);
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));

            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (150 * k)).getPacked()).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * start, (float) 0, (float) v * start).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
        }
        tessellator.draw();

        // circle
        /*
            __
            --
            __
         */

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i += 5) {
            int clr = getColor(settings, i);
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));

            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (255 * k)).getPacked()).next();
            bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) u * (middle - inGlowRadius), (float) 0, (float) v * (middle - inGlowRadius)).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
        }
        tessellator.draw();

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderCapUtils.reset("glow_circle");
        matrices.translate(-x, -y, -z);
        matrices.pop();
    }

    public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
        double x = prevposX + ((posX - prevposX) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = prevposY + ((posY - prevposY) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = prevposZ + ((posZ - prevposZ) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        return new Vec3d(x, y, z);
    }

    public static int getColor(ColorMode.ColorSettings settings, int stage) {
        return switch (settings.mode.get()) {
            case Custom -> settings.color.get().getPacked();
            case Rainbow -> Render2DUtils.rainbow(stage, 1f, 1f).getPacked();
            case TwoColor -> getColor2(settings, stage).getPacked();
            case Astolfo -> settings.astolfo.getColor(((stage + 90) / 360.));
        };
    }

    public static Color getColor2(ColorMode.ColorSettings settings, int offset) {
        return TwoColoreffect(settings.color.get(), settings.color2.get(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset * ((20f - settings.colorOffset.get()) / 200));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color((int) lerp((float) cl1.r / 255.0f, (float) cl2.r / 255.0f, val), (int) lerp((float) cl1.g / 255.0f, (float) cl2.g / 255.0f, val), (int) lerp((float) cl1.b / 255.0f, (float) cl2.b / 255.0f, val));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);
        vertexConsumer.vertex(model, x1, y1, z1).color(lineColor.r, lineColor.g, lineColor.b, lineColor.a).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        vertexConsumer.vertex(model, x2, y2, z2).color(lineColor.r, lineColor.g, lineColor.b, lineColor.a).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static class RenderInfo {
        public Render3DEvent event;
        public RenderMode renderMode;
        public ShapeMode shapeMode;

        public RenderInfo(Render3DEvent event, RenderMode renderMode, ShapeMode shapeMode) {
            this.event = event;
            this.renderMode = renderMode;
            this.shapeMode = shapeMode;
        }

        public RenderInfo(Render3DEvent event, RenderMode renderMode) {
            this.event = event;
            this.renderMode = renderMode;
        }
    }
}
