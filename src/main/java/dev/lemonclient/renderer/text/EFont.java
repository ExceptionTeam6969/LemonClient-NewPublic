package dev.lemonclient.renderer.text;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.render.font.FontRenderer;
import dev.lemonclient.renderer.Mesh;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.io.IOException;

public class EFont {
    public FontRenderer renderer;

    public EFont(int height, FontFace... face) {
        try {
            java.awt.Font[] fonts = new java.awt.Font[face.length];
            for (int i = 0; i < face.length; i++) {
                fonts[i] = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, face[i].toStream()).deriveFont(face[i].info.type().toJava(), height / 2f);
            }
            this.renderer = new FontRenderer(fonts, height);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
    }

    public double getWidth(String string, double scale, int length) {
        string = string.substring(0, length);
        return renderer.getWidth(string, scale);
    }

    public int getHeight() {
        return MathHelper.floor(renderer.fontHeight);
    }

    public double render(Mesh mesh, String string, double x, double y, Color color, double scale) {
        return x + renderer.drawString(mesh, RenderSystem.getModelViewStack(), string, (float) x, (float) y, color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f, scale);
    }
}
