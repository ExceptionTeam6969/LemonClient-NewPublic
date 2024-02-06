package dev.lemonclient.render.gui;

import dev.lemonclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;

import static dev.lemonclient.LemonClient.mc;

public abstract class Widget {
    protected GuiRenderer renderer;

    public double x, y, lastX, lastY, finalX, finalY;
    public boolean moving, smooth, debug;

    private double lastMouseX, lastMouseY;

    private int tickCount;

    protected double smoothSpeed = 0.1;

    public int widgetId;

    public Widget(double x, double y) {
        this.x = x;
        this.y = y;
        widgetId = 0;
    }

    public void preMouseClick(double mouseX, double mouseY, int button) {
        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        mouseClick(mouseX, mouseY, button);
    }

    public void mouseClick(double mouseX, double mouseY, int button) {
    }

    public final void preMouseMove(double mouseX, double mouseY) {
        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        mouseMove(mouseX, mouseY);
    }

    public void mouseMove(double mouseX, double mouseY) {
    }

    public final void preTick() {
        if (tickCount++ % 15 == 0) {
            moving = false;
            tickCount = 0;
        }

        if (lastX != x) {
            lastX = x;
            moving = true;
        }
        if (lastY != y) {
            lastY = y;
            moving = true;
        }
        this.tick();
    }

    public void tick() {
    }

    public final void preRender(GuiRenderer renderer, double mouseX, double mouseY, float tickDelta) {
        if (this.renderer == null) {
            this.renderer = renderer;
        }

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        if (Utils.rendering3D) {
            Utils.unscaledProjection();
        }

        if (smooth) {
            x = smooth(x, finalX);
            y = smooth(y, finalY);
        } else {
            x = finalX;
            y = finalY;
        }

        render(renderer, renderer.context, renderer.matrices, mouseX, mouseY, tickDelta);
        if (debug) {
            debugRender(renderer, renderer.context, renderer.matrices, mouseX, mouseY, tickDelta);
        }
        if (!Utils.rendering3D) {
            Utils.scaledProjection();
        }
    }

    public abstract void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta);

    public abstract void debugRender(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta);

    public void move(double x, double y) {
        finalX = x;
        finalY = y;
    }

    protected double smooth(double start, double end) {
        double speed = (end - start) * smoothSpeed;

        if (speed > 0) {
            speed = Math.max(smoothSpeed, speed);
            speed = Math.min(end - start, speed);
        } else if (speed < 0) {
            speed = Math.min(-smoothSpeed, speed);
            speed = Math.max(end - start, speed);
        }
        return start + speed;
    }

    protected float smooth(float start, float end) {
        float speed = (end - start) * (float) smoothSpeed;

        if (speed > 0) {
            speed = Math.max((float) smoothSpeed, speed);
            speed = Math.min(end - start, speed);
        } else if (speed < 0) {
            speed = Math.min(-(float) smoothSpeed, speed);
            speed = Math.max(end - start, speed);
        }
        return start + speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Widget widget = (Widget) o;

        return widgetId == widget.widgetId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(widgetId);
    }
}
