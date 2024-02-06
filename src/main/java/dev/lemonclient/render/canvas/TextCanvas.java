package dev.lemonclient.render.canvas;

import dev.lemonclient.render.helper.TextHelper;
import dev.lemonclient.utils.render.color.Color;

public abstract class TextCanvas extends MatrixCanvas implements TextHelper {
    protected double scale;

    public abstract void draw(String text, double x, double y, Color color, boolean shadow);

    public void draw(String text, double x, double y, Color color) {
        this.draw(text, x, y, color, false);
    }

    // Float
    public void draw(String text, float x, float y, Color color) {
        this.draw(text, x, (double) y, color);
    }

    public void draw(String text, float x, float y, int color) {
        this.draw(text, x, (double) y, new Color(color));
    }

    public void draw(String text, float x, float y, int red, int green, int blue, int alpha) {
        this.draw(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void draw(String text, float x, float y) {
        this.draw(text, x, (double) y, Color.WHITE);
    }

    public void draw(String text, float x, float y, int red, int green, int blue) {
        this.draw(text, x, y, red, green, blue, 255);
    }

    public void draw(String text, float x, float y, float red, float green, float blue, float alpha) {
        this.draw(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void draw(String text, float x, float y, float red, float green, float blue) {
        this.draw(text, x, y, red, green, blue, 1f);
    }

    // Int
    public void draw(String text, int x, int y, Color color) {
        this.draw(text, x, (double) y, color);
    }

    public void draw(String text, int x, int y, int color) {
        this.draw(text, x, (double) y, new Color(color));
    }

    public void draw(String text, int x, int y, int red, int green, int blue, int alpha) {
        this.draw(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void draw(String text, int x, int y, int red, int green, int blue) {
        this.draw(text, x, y, red, green, blue, 255);
    }

    public void draw(String text, int x, int y, float red, float green, float blue, float alpha) {
        this.draw(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void draw(String text, int x, int y, float red, float green, float blue) {
        this.draw(text, x, y, red, green, blue, 1f);
    }

    public void drawWShadow(String text, double x, double y, Color color) {
        this.draw(text, x, y, color, true);
    }

    // Float
    public void drawWShadow(String text, float x, float y, Color color) {
        this.drawWShadow(text, x, (double) y, color);
    }

    public void drawWShadow(String text, float x, float y, int color) {
        this.drawWShadow(text, x, (double) y, new Color(color));
    }

    public void drawWShadow(String text, float x, float y, int red, int green, int blue, int alpha) {
        this.drawWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawWShadow(String text, float x, float y, int red, int green, int blue) {
        this.drawWShadow(text, x, y, red, green, blue, 255);
    }

    public void drawWShadow(String text, float x, float y, float red, float green, float blue, float alpha) {
        this.drawWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawWShadow(String text, float x, float y, float red, float green, float blue) {
        this.drawWShadow(text, x, y, red, green, blue, 1f);
    }

    // Int
    public void drawWShadow(String text, int x, int y, Color color) {
        this.drawWShadow(text, x, (double) y, color);
    }

    public void drawWShadow(String text, int x, int y, int color) {
        this.drawWShadow(text, x, (double) y, new Color(color));
    }

    public void drawWShadow(String text, int x, int y, int red, int green, int blue, int alpha) {
        this.drawWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawWShadow(String text, int x, int y, int red, int green, int blue) {
        this.drawWShadow(text, x, y, red, green, blue, 255);
    }

    public void drawWShadow(String text, int x, int y, float red, float green, float blue, float alpha) {
        this.drawWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawWShadow(String text, int x, int y, float red, float green, float blue) {
        this.drawWShadow(text, x, y, red, green, blue, 1f);
    }

    // --------------------2----------------------
    public abstract void drawCenter(String text, double x, double y, Color color, boolean shadow);

    public void drawCenter(String text, double x, double y, Color color) {
        this.drawCenter(text, x, y, color, false);
    }

    // Float
    public void drawCenter(String text, float x, float y, Color color) {
        this.draw(text, x, (double) y, color);
    }

    public void drawCenter(String text, float x, float y, int color) {
        this.drawCenter(text, x, (double) y, new Color(color));
    }

    public void drawCenter(String text, float x, float y, int red, int green, int blue, int alpha) {
        this.drawCenter(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenter(String text, float x, float y, int red, int green, int blue) {
        this.drawCenter(text, x, y, red, green, blue, 255);
    }

    public void drawCenter(String text, float x, float y, float red, float green, float blue, float alpha) {
        this.drawCenter(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenter(String text, float x, float y, float red, float green, float blue) {
        this.drawCenter(text, x, y, red, green, blue, 1f);
    }

    // Int
    public void drawCenter(String text, int x, int y, Color color) {
        this.drawCenter(text, x, (double) y, color);
    }

    public void drawCenter(String text, int x, int y, int color) {
        this.drawCenter(text, x, (double) y, new Color(color));
    }

    public void drawCenter(String text, int x, int y, int red, int green, int blue, int alpha) {
        this.drawCenter(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenter(String text, int x, int y, int red, int green, int blue) {
        this.drawCenter(text, x, y, red, green, blue, 255);
    }

    public void drawCenter(String text, int x, int y, float red, float green, float blue, float alpha) {
        this.drawCenter(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenter(String text, int x, int y, float red, float green, float blue) {
        this.drawCenter(text, x, y, red, green, blue, 1f);
    }

    public void drawCenterWShadow(String text, double x, double y, Color color) {
        this.drawCenter(text, x, y, color, true);
    }

    // Float
    public void drawCenterWShadow(String text, float x, float y, Color color) {
        this.drawCenter(text, x, y, color, true);
    }

    public void drawCenterWShadow(String text, float x, float y, int color) {
        this.drawCenterWShadow(text, x, (double) y, new Color(color));
    }

    public void drawCenterWShadow(String text, float x, float y, int red, int green, int blue, int alpha) {
        this.drawCenterWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenterWShadow(String text, float x, float y, int red, int green, int blue) {
        this.drawCenterWShadow(text, x, y, red, green, blue, 255);
    }

    public void drawCenterWShadow(String text, float x, float y, float red, float green, float blue, float alpha) {
        this.drawCenterWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenterWShadow(String text, float x, float y, float red, float green, float blue) {
        this.drawCenterWShadow(text, x, y, red, green, blue, 1f);
    }

    // Int
    public void drawCenterWShadow(String text, int x, int y, Color color) {
        this.drawCenterWShadow(text, x, (double) y, color);
    }

    public void drawCenterWShadow(String text, int x, int y, int color) {
        this.drawCenterWShadow(text, x, (double) y, new Color(color));
    }

    public void drawCenterWShadow(String text, int x, int y, int red, int green, int blue, int alpha) {
        this.drawCenterWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenterWShadow(String text, int x, int y, int red, int green, int blue) {
        this.drawCenterWShadow(text, x, y, red, green, blue, 255);
    }

    public void drawCenterWShadow(String text, int x, int y, float red, float green, float blue, float alpha) {
        this.drawCenterWShadow(text, x, (double) y, new Color(red, green, blue, alpha));
    }

    public void drawCenterWShadow(String text, int x, int y, float red, float green, float blue) {
        this.drawCenterWShadow(text, x, y, red, green, blue, 1f);
    }

    public abstract void drawRectCenter(String text, double x, double y, double width, double height, Color color, boolean shadow);

    public abstract double width(String text, boolean shadow);

    public double width(String text) {
        return this.width(text, false);
    }

    public double widthWShadow(String text) {
        return this.width(text, true);
    }

    public abstract double height(String text, boolean shadow);

    public double height(String text) {
        return this.height(text, false);
    }

    public double heightWShadow(String text) {
        return this.height(text, true);
    }

    public abstract void scale(double next);

    public abstract void begin();

    public abstract void end();
}
