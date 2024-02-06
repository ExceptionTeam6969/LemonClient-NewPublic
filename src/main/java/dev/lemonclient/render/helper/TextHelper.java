package dev.lemonclient.render.helper;

import dev.lemonclient.render.canvas.TextCanvas;
import org.joml.Vector2d;

public interface TextHelper {
    default Vector2d calcCenter(TextCanvas canvas, String text, double x, double y, boolean shadow) {
        double strWid = canvas.width(text, shadow);
        double strHei = canvas.height(text, shadow);
        return new Vector2d(x - strWid / 2, y - strHei / 2);
    }
}
