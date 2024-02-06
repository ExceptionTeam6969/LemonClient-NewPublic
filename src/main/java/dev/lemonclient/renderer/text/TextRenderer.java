package dev.lemonclient.renderer.text;

import dev.lemonclient.renderer.Fonts;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import top.fl0wowp4rty.phantomshield.annotations.license.VirtualizationLock;

public interface TextRenderer {
    @VirtualizationLock
    static TextRenderer get() {
        return Config.get().customFont.get() ? Fonts.RENDERER : VanillaTextRenderer.INSTANCE;
    }

    @VirtualizationLock
    void setAlpha(double a);

    @VirtualizationLock
    void begin(double scale, boolean scaleOnly, boolean big);

    @VirtualizationLock
    default void begin(double scale) {
        begin(scale, false, false);
    }

    @VirtualizationLock
    default void begin() {
        begin(1, false, false);
    }

    @VirtualizationLock
    default void beginBig() {
        begin(1, false, true);
    }

    @VirtualizationLock
    double getWidth(String text, int length, boolean shadow);

    @VirtualizationLock
    default double getWidth(String text, boolean shadow) {
        return getWidth(text, text.length(), shadow);
    }

    @VirtualizationLock
    default double getWidth(String text) {
        return getWidth(text, text.length(), false);
    }

    @VirtualizationLock
    double getHeight(boolean shadow);

    @VirtualizationLock
    default double getHeight() {
        return getHeight(false);
    }

    @VirtualizationLock
    double render(String text, double x, double y, Color color, boolean shadow);

    @VirtualizationLock
    default double render(String text, double x, double y, Color color) {
        return render(text, x, y, color, false);
    }

    @VirtualizationLock
    boolean isBuilding();

    @VirtualizationLock
    default void end() {
        end(null);
    }

    @VirtualizationLock
    void end(MatrixStack matrices);
}
