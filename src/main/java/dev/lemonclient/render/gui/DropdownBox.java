package dev.lemonclient.render.gui;

import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class DropdownBox<T extends Enum<?>> extends RoundBox {
    private T[] values;
    public T current;
    private final List<String> suggestions;
    private final Map<String, T> enums;

    public boolean show;
    public float scale, finalScale;

    private static double valueWidth = 100;
    private final double valueHeight = 35;

    private final Color hoveredColor;
    private final Color defaultHoverColor, defaultColor;

    private double scissorX, scissorY, nextScissorX, nextScissorY;
    private double currentHeight, nextHeight;
    private int alpha, nextAlpha, hoverAlpha, nextHoverAlpha;
    private final BiConsumer<T, T> onChanged;


    public DropdownBox(double x, double y, double width, double height, T defaultValue, double radius, Color color, Color hoveredColor) {
        this(x, y, width, height, defaultValue, radius, color, hoveredColor, null);
    }

    public DropdownBox(double x, double y, double width, double height, T defaultValue, double radius, Color color, Color hoveredColor, BiConsumer<T, T> onChanged) {
        super(x, y, width, height, radius, color);
        this.hoveredColor = hoveredColor;
        this.onChanged = onChanged;
        defaultColor = color;
        defaultHoverColor = hoveredColor;
        alpha = defaultColor.a;
        nextAlpha = defaultColor.a;
        hoverAlpha = defaultHoverColor.a;
        nextHoverAlpha = defaultHoverColor.a;

        try {
            values = (T[]) defaultValue.getClass().getMethod("values").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        suggestions = new ArrayList<>(values.length);
        enums = new HashMap<>(values.length);
        for (T value : values) {
            suggestions.add(value.toString());
            enums.put(value.toString(), value);
        }

        current = defaultValue;
    }

    @Override
    public void mouseClick(double mouseX, double mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);

        if (show) {
            double offset = currentHeight / suggestions.size();
            double valX = this.x;
            double valY = this.y;
            if (isMouseHoveringRect(valX, valY, valueWidth, offset, mouseX, mouseY)) {
                show = false;
            }
            valY += offset;
            for (String val : suggestions) {
                if (val.equals(current.name())) continue;

                if (isMouseHoveringRect(valX, valY, valueWidth, offset, mouseX, mouseY)) {
                    set(val);
                    show = false;
                }
                valY += offset;
            }
        } else {
            show = isMouseHoveringRect(this.x, this.y, this.width, this.height, mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        renderer.mesh = true;
        renderer.vanillaText = true;
        this.smoothSpeed = 0.5;

        MSAAFramebuffer.use(() -> {
            nextHoverAlpha = defaultHoverColor.a;

            if (show) {
                hovered = isMouseHoveringRect(x, y, width, height, mouseX, mouseY);
                scale = smooth(scale, 10f);
                nextHeight = valueHeight * suggestions.size();
            } else {
                hovered = isMouseHoveringRect(x, y, valueWidth, valueHeight, mouseX, mouseY);
                scale = smooth(scale, 0f);
                nextHeight = valueHeight;
            }
            nextAlpha = defaultColor.a;

            finalScale = scale / 10f;

            this.height = currentHeight;
            this.width = valueWidth;

            for (String s : suggestions) {
                double width = renderer.width(s, 2);
                if (5 + width + 5 > this.width) {
                    this.width = 5 + width + 5;
                    valueWidth = 5 + width + 5;
                }
            }

            if (show) {
                double offset = currentHeight / suggestions.size();

                renderer.scissorStart();
                renderer.scissor(x, y, width, currentHeight = smooth(currentHeight, nextHeight));
                renderer.drawRound(x, y, width, currentHeight, radius, color);
                renderer.scissorEnd();

                double yy = this.y;
                if (isMouseHoveringRect(this.x, yy, valueWidth, offset, mouseX, mouseY)) {
                    nextScissorX = this.x;
                    nextScissorY = this.y;
                }
                renderer.centerText(current.name(), this.x, yy, valueWidth, offset, 2, Color.BLACK.a(alpha = (int) smooth(alpha, nextAlpha)));
                yy += offset;
                for (String s : suggestions) {
                    if (s.equals(current.name())) continue;

                    if (isMouseHoveringRect(this.x, yy, valueWidth, offset, mouseX, mouseY)) {
                        nextScissorX = this.x;
                        nextScissorY = yy;

                        renderer.drawRound(scissorX = smooth(scissorX, nextScissorX) + 2, scissorY = smooth(scissorY, nextScissorY) + 2, valueWidth - 8, valueHeight - 8, radius, hoveredColor);
                    }

                    renderer.centerText(s, this.x, yy, valueWidth, offset, 2, Color.BLACK.a(alpha = (int) smooth(alpha, nextAlpha)));

                    yy += offset;
                }
            } else {
                renderer.drawRound(x, y, width, currentHeight = smooth(currentHeight, nextHeight), radius, color.a(alpha = (int) smooth(alpha, nextAlpha)));
                if (hovered) {
                    renderer.drawRound(x + 4, y + 4, width - 8, currentHeight - 8, radius, hoveredColor.a(hoverAlpha = (int) smooth(hoverAlpha, nextHoverAlpha)));
                }
                renderer.centerText(current.name(), x, y, width, currentHeight, 2, Color.BLACK.a(alpha));
            }
        });
    }

    @Override
    public void debugRender(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        super.debugRender(renderer, context, matrices, mouseX, mouseY, tickDelta);
    }

    public T get() {
        return current;
    }

    public DropdownBox<T> set(T current) {
        this.current = current;
        return this;
    }

    public DropdownBox<T> set(String name) {
        for (Map.Entry<String, T> entry : enums.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                if (onChanged != null) {
                    onChanged.accept(current, entry.getValue());
                }
                current = entry.getValue();
            }
        }

        return this;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public Map<String, T> getEnums() {
        return enums;
    }
}
