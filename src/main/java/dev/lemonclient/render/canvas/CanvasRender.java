package dev.lemonclient.render.canvas;

import dev.lemonclient.render.MeshRender;
import dev.lemonclient.render.utils.BufferTexture;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public abstract class CanvasRender extends MatrixCanvas implements MeshRender {
    private boolean msaa = false;
    private boolean immutableMode = true;

    public void enableImmutableMode() {
        setImmutableMode(true);
    }

    public void disableImmutableMode() {
        setImmutableMode(false);
    }

    public boolean isImmutableMode() {
        return immutableMode;
    }

    public void setImmutableMode(boolean immutableMode) {
        this.immutableMode = immutableMode;
    }

    public boolean isMsaa() {
        return msaa;
    }

    public void enableMsaa() {
        msaa = true;
    }

    public void setMsaa(boolean mode) {
        msaa = mode;
    }

    public void disableMsaa() {
        msaa = false;
    }

    public void drawRect(double x, double y, double width, double height, Color color) {
        drawRect((float) x, (float) y, (float) width, (float) height, color);
    }

    public void drawRect(int x, int y, int width, int height, Color color) {
        drawRect(x, y, width, (double) height, color);
    }

    public abstract void drawRect(float x, float y, float width, float height, Color color);

    public void drawRect(int x, int y, int width, int height, int color) {
        drawRect(x, y, width, (double) height, color);
    }

    public void drawRect(double x, double y, double width, double height, int color) {
        drawRect(x, y, width, height, new Color(color));
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, (double) height, color);
    }

    public void drawRoundRect(double x, double y, double width, double height, double radius, Color color) {
        drawRoundRect((float) x, (float) y, (float) width, (float) height, (float) radius, color);
    }

    public void drawRoundRect(int x, int y, int width, int height, int radius, Color color) {
        drawRoundRect((double) x, y, width, height, radius, color);
    }

    public abstract void drawRoundRect(float x, float y, float width, float height, float radius, Color color);

    public void drawRoundRect(int x, int y, int width, int height, int radius, int color) {
        drawRoundRect((double) x, y, width, height, radius, color);
    }

    public void drawRoundRect(double x, double y, double width, double height, double radius, int color) {
        drawRoundRect(x, y, width, height, radius, new Color(color));
    }

    public void drawRoundRect(float x, float y, float width, float height, float radius, int color) {
        drawRoundRect((double) x, y, width, height, radius, color);
    }

    public void drawRoundRectLine(double x, double y, double width, double height, double radius, double lineWidth, Color color) {
        drawRoundRectLine((float) x, (float) y, (float) width, (float) height, (float) radius, (float) lineWidth, color);
    }

    public void drawRoundRectLine(int x, int y, int width, int height, int radius, int lineWidth, Color color) {
        drawRoundRectLine((double) x, y, width, height, radius, lineWidth, color);
    }

    public abstract void drawRoundRectLine(float x, float y, float width, float height, float radius, float lineWidth, Color color);

    public void drawRoundRectLine(int x, int y, int width, int height, int radius, int lineWidth, int color) {
        drawRoundRectLine((double) x, y, width, height, radius, lineWidth, color);
    }

    public void drawRoundRectLine(double x, double y, double width, double height, double radius, double lineWidth, int color) {
        drawRoundRectLine(x, y, width, height, radius, lineWidth, new Color(color));
    }

    public void drawRoundRectLine(float x, float y, float width, float height, float radius, double lineWidth, int color) {
        drawRoundRectLine((double) x, y, width, height, radius, lineWidth, color);
    }

    public abstract void drawRoundRectOutline(float x, float y, float width, float height, float radius, float outlineWidth, Color inColor, Color outColor);

    public void drawRoundRectOutline(double x, double y, double width, double height, double radius, double outlineWidth, Color inColor, Color outColor) {
        drawRoundRectOutline((float) x, (float) y, (float) width, (float) height, (float) radius, (float) outlineWidth, inColor, outColor);
    }

    public void drawRoundRectOutline(int x, int y, int width, int height, int radius, int outlineWidth, Color inColor, Color outColor) {
        drawRoundRectOutline((double) x, y, width, height, radius, outlineWidth, inColor, outColor);
    }

    public void drawRoundRectOutline(int x, int y, int width, int height, int radius, int outlineWidth, int inColor, int outColor) {
        drawRoundRectOutline((double) x, y, width, height, radius, outlineWidth, inColor, outColor);
    }

    public void drawRoundRectOutline(double x, double y, double width, double height, double radius, double outlineWidth, int inColor, int outColor) {
        drawRoundRectOutline(x, y, width, height, radius, outlineWidth, new Color(inColor), new Color(outColor));
    }

    public void drawRoundRectOutline(float x, float y, float width, float height, float radius, double outlineWidth, int inColor, int outColor) {
        drawRoundRectOutline((double) x, y, width, height, radius, outlineWidth, inColor, outColor);
    }

    public void drawLine(double x1, double y1, double x2, double y2, Color color) {
        drawLine((float) x1, (float) y1, (float) x2, (float) y2, color);
    }

    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        drawLine((double) x1, y1, x2, y2, color);
    }

    public abstract void drawLine(float x1, float y1, float x2, float y2, Color color);

    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        drawLine((double) x1, y1, x2, y2, color);
    }

    public void drawLine(double x1, double y1, double x2, double y2, int color) {
        drawLine(x1, y1, x2, y2, new Color(color));
    }

    public void drawLine(float x1, float y1, float x2, float y2, int color) {
        drawLine((double) x1, y1, x2, y2, color);
    }

    public void drawCircle(double x, double y, double radius, Color color) {
        drawCircle((float) x, (float) y, (float) radius, color);
    }

    public void drawCircle(int x, int y, int radius, Color color) {
        drawCircle((double) x, y, radius, color);
    }

    public abstract void drawCircle(float x, float y, float radius, Color color);

    public void drawCircle(int x, int y, int radius, int color) {
        drawCircle((double) x, y, radius, color);
    }

    public void drawCircle(double x, double y, double radius, int color) {
        drawCircle(x, y, radius, new Color(color));
    }

    public void drawCircle(float x, float y, float radius, int color) {
        drawCircle((double) x, y, radius, color);
    }

    public abstract void drawTexture(Identifier id, double x, double y, double width, double height, Color color);

    public abstract BufferTexture setupImage(BufferedImage id);

    public abstract void drawTexture(NativeImage id, double x, double y, double width, double height, Color color);

    public abstract void drawTexture(NativeImageBackedTexture id, double x, double y, double width, double height, Color color);

    public abstract void render(MatrixStack matrix);

    public void render() {
        this.render(getMatrixStack());
    }

}
