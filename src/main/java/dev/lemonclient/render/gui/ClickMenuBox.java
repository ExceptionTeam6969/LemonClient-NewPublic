package dev.lemonclient.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.function.Consumer;

public class ClickMenuBox extends RoundBox {
    public boolean show;

    private final Color hoveredColor;
    private final Color defaultHoverColor, defaultColor;

    private double showX, showY;

    private float scale, nextScale, finalScale;
    private double maxWidth;
    private final double maxHeight;

    private final Map<String, Consumer<ClickMenuBox>> menus;

    //private final WidgetContainer container;
    private final TextBox[] textBoxes;

    public ClickMenuBox(double x, double y, double width, double height, double radius, Color color, Color hoveredColor, Map<String, Consumer<ClickMenuBox>> menus) {
        super(x, y, width, height, radius, color);
        this.hoveredColor = hoveredColor;
        defaultColor = color;
        defaultHoverColor = hoveredColor;
        //container = new WidgetContainer(renderer, false, true);
        textBoxes = new TextBox[menus.size()];
        int tid = 0;
        for (Map.Entry<String, Consumer<ClickMenuBox>> entry : menus.entrySet()) {
            textBoxes[tid] = new TextBox(entry.getKey(), 10, 10, 10, 10, Color.WHITE);
            //container.addWidget(textBoxes[tid]);
            tid++;
        }

        maxHeight = 35 * textBoxes.length;

        this.menus = menus;
    }

    @Override
    public void mouseClick(double mouseX, double mouseY, int button) {
        switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                if (isMouseHoveringRect(showX, showY, maxWidth, maxHeight, mouseX, mouseY)) {
                    if (show) {
                        double yy = this.y;
                        for (Map.Entry<String, Consumer<ClickMenuBox>> w : menus.entrySet()) {
                            if (isMouseHoveringRect(this.x, yy, maxWidth, 35, mouseX, mouseY)) {
                                w.getValue().accept(this);
                            }
                            yy += 35;
                        }
                    }
                } else {
                    hiddenMenu();
                }
            }
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                if (!isMouseHoveringRect(showX, showY, maxWidth, maxHeight, mouseX, mouseY) && show) {
                    showX = mouseX;
                    showY = mouseY;
                    break;
                }

                show = true;
                showX = mouseX;
                showY = mouseY;
                nextScale = 30;
            }
        }

        //container.mouseClick(mouseX, mouseY, button);
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack m, double mouseX, double mouseY, float tickDelta) {
        this.x = showX;
        this.y = showY;
        this.smoothSpeed = 0.1;
        scale = smooth(scale, nextScale);
        finalScale = scale / 30f;

        MatrixStack matrices = RenderSystem.getModelViewStack();
        int menuAlpha = (int) Math.max(this.defaultColor.a * finalScale, 255);

        MSAAFramebuffer.use(() -> {
            if (show) {
                renderer.drawRound(x, y, maxWidth, maxHeight, 5, new Color(this.color.r, this.color.g, this.color.b, menuAlpha));
                double yy = y;
                for (TextBox textBox : textBoxes) {
                    double width = 10 + renderer.width(textBox.text, 2) + 10;
                    if (width > maxWidth) {
                        maxWidth = width;
                    }

                    if (isMouseHoveringRect(x, yy, maxWidth, 35, mouseX, mouseY)) {
                        renderer.drawRound(x + 4, yy + 4, maxWidth - 8, 35 - 8, radius, hoveredColor);
                    }
                    renderer.centerText(textBox.text, x, yy, maxWidth, 35, 2, Color.WHITE);
                    yy += 35;
                }
                //container.render(context, mouseX, mouseY, tickDelta);
            }
        });
    }

    public void hiddenMenu() {
        show = false;
        nextScale = 0;
    }
}
