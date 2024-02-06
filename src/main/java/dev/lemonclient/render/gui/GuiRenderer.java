package dev.lemonclient.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import static dev.lemonclient.LemonClient.mc;
import static org.lwjgl.opengl.GL11.glScissor;

public class GuiRenderer {
    public DrawContext context;
    public MatrixStack matrices;
    private MatrixStack MC_MATRIX;

    private Renderer2D MESH_COLOR;
    private Renderer2D MESH_TEXTURE;
    private BufferBuilder MC_BUFFER;
    private TextRenderer TEXT;

    public boolean mesh = false, depthTest, vanillaText;
    public float lineWidth = 1f;

    public void setup(DrawContext context) {
        this.context = context;
        this.MC_MATRIX = RenderSystem.getModelViewStack();
        this.matrices = context.getMatrices();

        MESH_COLOR = Renderer2D.COLOR;
        MESH_TEXTURE = Renderer2D.TEXTURE;
        MC_BUFFER = Tessellator.getInstance().getBuffer();
        TEXT = TextRenderer.get();
    }

    public GuiRenderer(DrawContext context) {
        this.context = context;
        this.MC_MATRIX = RenderSystem.getModelViewStack();
        this.matrices = context.getMatrices();

        MESH_COLOR = Renderer2D.COLOR;
        MESH_TEXTURE = Renderer2D.TEXTURE;
        MC_BUFFER = Tessellator.getInstance().getBuffer();
        TEXT = TextRenderer.get();
    }

    public void drawRound(double x, double y, double width, double height, double radius, Color color) {
        if (mesh) {
            MESH_COLOR.begin();
            MESH_COLOR.quadRounded(x, y, width, height, color, radius, true);
            MESH_COLOR.render(matrices);
        } else {
            preDraw();
            Render2DUtils.drawRoundShader(matrices, (float) x, (float) y, (float) width, (float) height, (float) radius, color);
            postDraw();
        }
    }

    public void drawRoundRect(double x, double y, double width, double height, double radius, Color color) {
        drawRound(x, y, width, height, radius, color);
    }

    public void drawRect(double x, double y, double width, double height, Color color) {
        if (mesh) {
            MESH_COLOR.begin();
            MESH_COLOR.quad(x, y, width, height, color);
            MESH_COLOR.render(matrices);
        } else {
            preDraw();
            Render2DUtils.drawRectShader(matrices, (float) x, (float) y, (float) width, (float) height, color);
            postDraw();
        }
    }

    public void drawLine(double x, double y, double x1, double y1, Color color) {
        if (mesh) {
            MESH_COLOR.begin();
            MESH_COLOR.line(x, y, x1, y1, color);
            MESH_COLOR.render(matrices);
        } else {
            preDraw();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            MC_BUFFER.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);
            MC_BUFFER.vertex(matrix, (float) x, (float) y, 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            MC_BUFFER.vertex(matrix, (float) x1, (float) y1, 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            BufferRenderer.drawWithGlobalProgram(MC_BUFFER.end());
            postDraw();
        }
    }

    public void drawTexture(Identifier id, double x, double y, double width, double height, Color color) {
        if (mesh) {
            GL.bindTexture(id);
            MESH_TEXTURE.begin();
            MESH_TEXTURE.quad(x, y, width, height, color);
            MESH_TEXTURE.render(matrices);
        } else {
            preDraw();
            RenderSystem.setShaderTexture(0, id);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            MC_BUFFER.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            MC_BUFFER.vertex(matrix, (float) x, (float) y, 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            MC_BUFFER.vertex(matrix, (float) x, (float) (y + height), 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            MC_BUFFER.vertex(matrix, (float) (x + width), (float) (y + height), 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            MC_BUFFER.vertex(matrix, (float) (x + width), (float) y, 0).color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f).next();
            BufferRenderer.drawWithGlobalProgram(MC_BUFFER.end());
            postDraw();
        }
    }

    private void preDraw() {
        GL.saveState();

        if (this.depthTest) GL.enableDepth();
        else GL.disableDepth();
        GL.enableBlend();
        GL.disableCull();
        GL.enableLineSmooth(1);
        RenderSystem.lineWidth(this.lineWidth);
    }

    private void postDraw() {
        GL.restoreState();
    }

    public void scissorStart() {
        GL.enableScissorTest();
    }

    public void scissor(double x, double y, double width, double height, Runnable task) {
        scissorStart();
        scissor(x, y, width, height);
        task.run();
        scissorEnd();
    }

    public void scissor(double x, double y, double width, double height) {
        if (width < 0) width = 0;
        if (height < 0) height = 0;

        x = (int) Math.round(x);
        y = (int) Math.round(y);
        width = (int) Math.round(width);
        height = (int) Math.round(height);

        glScissor((int) x, (int) (Utils.getWindowHeight() - y - height), (int) width, (int) height);
    }

    public void scissorEnd() {
        GL.disableScissorTest();
    }

    public void text(String s, double x, double y, Color color) {
        this.text(s, x, y, 1.0, color);
    }

    public void centerText(String s, double x, double y, Color color) {
        this.centerText(s, x, y, 1.0, color);
    }

    public void centerText(String s, double x, double y, double width, double height, Color color) {
        this.centerText(s, x, y, width, height, 1.0, color);
    }

    public void text(String s, double x, double y, double scale, Color color) {
        if (vanillaText) {
            matrices.push();
            x += 0.5 * scale;
            y += 0.5 * scale;
            matrices.scale((float) scale, (float) scale, 1);
            context.drawText(mc.textRenderer, s, (int) (x / scale), (int) (y / scale), color.getPacked(), false);
            matrices.pop();
            return;
        }
        TEXT.begin(scale);
        TEXT.render(s, x, y, color);
        TEXT.end(matrices);
    }

    public void centerText(String s, double x, double y, double scale, Color color) {
        if (vanillaText) {
            double textWidth = width(s, scale);
            double textHeight = height(scale);
            x -= textWidth / 2;
            y -= textHeight / 2;
            text(s, x, y, scale, color);
            return;
        }
        TEXT.begin(scale);
        double textWidth = TEXT.getWidth(s);
        double textHeight = TEXT.getHeight();
        x -= textWidth / 2;
        y -= textHeight / 2;
        TEXT.render(s, x, y, color);
        TEXT.end(matrices);
    }

    public void centerText(String s, double x, double y, double width, double height, double scale, Color color) {
        if (vanillaText) {
            double textWidth = width(s, scale);
            double textHeight = height(scale);
            text(s, x + (width / 2) - textWidth / 2, y + (height / 2) - textHeight / 2, scale, color);
            return;
        }
        TEXT.begin(scale);
        double textWidth = TEXT.getWidth(s);
        double textHeight = TEXT.getHeight();
        TEXT.render(s, x + (width / 2) - textWidth / 2, y + (height / 2) - textHeight / 2, color);
        TEXT.end(matrices);
    }

    public double width(String message, double scale) {
        double width;
        if (vanillaText) {
            width = mc.textRenderer.getWidth(message) * scale;
        } else {
            TEXT.begin(scale);
            width = TEXT.getWidth(message);
            TEXT.end(matrices);
        }
        return width;
    }

    public double height(double scale) {
        double height = 2;
        if (vanillaText) {
            height = mc.textRenderer.fontHeight * scale;
        } else {
            TEXT.begin(scale);
            height = TEXT.getHeight();
            TEXT.end(matrices);
        }
        return height;
    }
}
