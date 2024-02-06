package dev.lemonclient.gui.screen.harvest;
/*
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.addon.LemonClient;
import dev.lemonclient.canvas.render.CanvasRender;
import dev.lemonclient.canvas.render.MeshCanvas;
import dev.lemonclient.canvas.render.TextCanvas;
import dev.lemonclient.LemonClient;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.systems.modules.Category;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class Component {
    public DrawContext context;
    public CanvasRender renderer;
    public TextCanvas vanillaText;
    public TextCanvas clientText;
    protected final MinecraftClient mc = LemonClient.mc;

    public abstract void render(int mouseX, int mouseY);

    public abstract void mouseDown(int mouseX, int mouseY, int button);

    public abstract void mouseUp(int mouseX, int mouseY);

    public abstract void keyPress(int key);

    public abstract void close();

    public void drawCheckBox(boolean checked, int x, int y) {
        MatrixStack stacks = renderer.getMatrixStack();
        stacks.scale(1.0F, 1.0F, 1.0F);
        Identifier identifier = new Identifier(LemonClient.MOD_ID, checked ? "checked.png" : "unchecked.png");
        renderer.drawTexture(identifier, x, y, 48, 48, new Color(10, 10, 10, 10));
    }

    public void drawEar(boolean left, int x, int y) {
        MatrixStack stack = renderer.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);
        Identifier ear = new Identifier(LemonClient.MOD_ID, left ? "left_ear.png" : "right_ear.png");
        renderer.drawTexture(ear,x, y, 35, 41, new Color(20, 20, 20));
    }

    public void drawOutline(int x, int y, int w, int h) {
        int color = new Color(255, 255, 255, 200).getPacked();
        normalOutline(x, y, w, h, color);

        switch (outline.get()) {
            case "Normal" -> normalOutline(x, y, w, h, color);
            case "Rounded" -> roundedOutline(x, y, w, h, color);
        }
    }

    private void roundedOutline(int x, int y, int w, int h, int color) {
        renderer.drawRect(x + 1, y + 1, 2, h, color);
        drawLine(x, y, w, h, color);
        renderer.drawRect(x + w - 1, y + 1, w - 2, h, color);
        renderer.drawRect(x + 2, y + h, w - 2, h + 1, color);
    }

    private void normalOutline(int x, int y, int w, int h, int color) {
        renderer.drawRect(x + 1, y + 1, 2, h, color);
        drawLine(x, y, w, h, color);
        renderer.drawRect(x + w - 1, y + 1, w - 2, h + 1, color);
        renderer.drawRect(x + 1, y + h, w - 2,  h + 1, color);
    }

    public void drawLine(int x, int y, int w, int h, int color) {
        renderer.drawRect(x + 1, y + 1, w - 1, y, color);


        switch (outline.get()) {
            case "Normal" -> renderer.drawRect(x + 1, y + 1, w - 1, y, color);
            case "Rounded" -> renderer.drawRect(x + 2, y + 1, w - 2, y, color);
        }
    }

    public void drawGradient(int left, int top, int right, int bottom, Color startColor, Color endColor) {
        MatrixStack st = renderer.getMatrixStack();
        st.scale(1.0F, 1.0F, 1.0F);
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(left, top, right - left, bottom - top, startColor,startColor, endColor,endColor);
        Renderer2D.COLOR.render(st);
    }

    public void drawFlat(int left, int top, int right, int bottom, Color startColor) {
        MatrixStack st = new MatrixStack();
        st.scale(1.0F, 1.0F, 1.0F);
        drawGradient(left,top,right,bottom,startColor,startColor);
    }
}
*/
