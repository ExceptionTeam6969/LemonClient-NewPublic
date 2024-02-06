package dev.lemonclient.render.font;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Mesh;
import dev.lemonclient.utils.render.color.Color;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.Closeable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.lemonclient.LemonClient.mc;

public class FontRenderer implements Closeable {
    private static final Char2IntArrayMap colorCodes = new Char2IntArrayMap() {{
        put('0', 0x000000);
        put('1', 0x0000AA);
        put('2', 0x00AA00);
        put('3', 0x00AAAA);
        put('4', 0xAA0000);
        put('5', 0xAA00AA);
        put('6', 0xFFAA00);
        put('7', 0xAAAAAA);
        put('8', 0x555555);
        put('9', 0x5555FF);
        put('A', 0x55FF55);
        put('B', 0x55FFFF);
        put('C', 0xFF5555);
        put('D', 0xFF55FF);
        put('E', 0xFFFF55);
        put('F', 0xFFFFFF);
    }};

    private static final int BLOCK_SIZE = 256;
    private static final Object2ObjectArrayMap<Identifier, ObjectList<DrawEntry>> GLYPH_PAGE_CACHE = new Object2ObjectArrayMap<>();
    private final float originalSize;
    private final ObjectList<GlyphMap> maps = new ObjectArrayList<>();
    private final Char2ObjectArrayMap<Glyph> allGlyphs = new Char2ObjectArrayMap<>();
    private Font[] fonts;
    private static final char RND_START = 'a';
    private static final char RND_END = 'z';
    private static final Random RND = new Random();

    public double fontHeight;

    public FontRenderer(Font[] fonts, float sizePx) {
        Preconditions.checkArgument(fonts.length > 0, "fonts.length == 0");
        this.originalSize = sizePx;
        init(fonts, sizePx);

        this.fontHeight = getStringHeight("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    private static int floorNearestMulN(int x, int n) {
        return n * (int) Math.floor((double) x / (double) n);
    }


    public static String stripControlCodes(String text) {
        char[] chars = text.toCharArray();
        StringBuilder f = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '§') {
                i++;
                continue;
            }
            f.append(c);
        }
        return f.toString();
    }

    private void init(Font[] fonts, float sizePx) {
        this.fonts = new Font[fonts.length];
        for (int i = 0; i < fonts.length; i++) {
            this.fonts[i] = fonts[i].deriveFont(sizePx);
        }
    }

    private GlyphMap generateMap(char from, char to) {
        GlyphMap gm = new GlyphMap((int) originalSize, from, to, this.fonts, randomIdentifier());
        maps.add(gm);
        return gm;
    }

    private Glyph locateGlyph0(char glyph) {
        for (GlyphMap map : maps) {
            if (map.contains(glyph)) {
                return map.getGlyph(glyph);
            }
        }
        int base = floorNearestMulN(glyph, BLOCK_SIZE);
        GlyphMap glyphMap = generateMap((char) base, (char) (base + BLOCK_SIZE));
        return glyphMap.getGlyph(glyph);
    }

    private Glyph locateGlyph1(char glyph) {
        return allGlyphs.computeIfAbsent(glyph, this::locateGlyph0);
    }

    public double drawString(Mesh mesh, MatrixStack stack, String s, float x, float y, float r, float g, float b, float a, double scale) {
        if (s.isEmpty())
            return 0.0;

        float r2 = r, g2 = g, b2 = b;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        double height = getStringHeight(s);
        if (height > this.fontHeight) {
            this.fontHeight = height;
        }

        stack.push();
        stack.translate(x, y, 0);
        //stack.translate(x,y,0);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        char[] chars = s.toCharArray();
        double xOffset = 0;
        double yOffset = 0;
        boolean inSel = false;
        int lineStart = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (inSel) {
                inSel = false;
                char c1 = Character.toUpperCase(c);
                if (colorCodes.containsKey(c1)) {
                    int ii = colorCodes.get(c1);
                    int[] col = RGBIntToRGB(ii);
                    r2 = col[0] / 255f;
                    g2 = col[1] / 255f;
                    b2 = col[2] / 255f;
                } else if (c1 == 'R') {
                    r2 = r;
                    g2 = g;
                    b2 = b;
                }
                continue;
            }
            if (c == '§') {
                inSel = true;
                continue;
            } else if (c == '\n') {
                yOffset += getStringHeight(s.substring(lineStart, i)) * scale;
                xOffset = 0;
                lineStart = i + 1;
                continue;
            }
            Glyph glyph = locateGlyph1(c);
            if (glyph == null)
                continue;
            if (glyph.value() != ' ') {
                Identifier i1 = glyph.owner().bindToTexture;
                DrawEntry entry = new DrawEntry(xOffset, yOffset, r2, g2, b2, glyph);
                GLYPH_PAGE_CACHE.computeIfAbsent(i1, integer -> new ObjectArrayList<>()).add(entry);
            }
            xOffset += glyph.width() * scale;
        }

        for (Identifier identifier : GLYPH_PAGE_CACHE.keySet()) {
            GL.bindTexture(identifier);
            RenderSystem.setShaderTexture(0, identifier);
            List<DrawEntry> objects = GLYPH_PAGE_CACHE.get(identifier);

            mesh.begin();
            for (DrawEntry object : objects) {
                double xo = object.atX;
                double yo = object.atY;
                float cr = object.r;
                float cg = object.g;
                float cb = object.b;
                Glyph glyph = object.toDraw;
                GlyphMap owner = glyph.owner();
                double w = glyph.width() * scale;
                double h = glyph.height() * scale;
                float u1 = (float) glyph.u() / owner.width;
                float v1 = (float) glyph.v() / owner.height;
                float u2 = (float) (glyph.u() + glyph.width()) / owner.width;
                float v2 = (float) (glyph.v() + glyph.height()) / owner.height;
                Color color = new Color(cr, cg, cb, a);

                mesh.quad(
                    mesh.vec2(xo, (yo + h)).vec2(u1, v2).color(color).next(),
                    mesh.vec2((xo + w), (yo + h)).vec2(u2, v2).color(color).next(),
                    mesh.vec2((xo + w), yo).vec2(u2, v1).color(color).next(),
                    mesh.vec2(xo, yo).vec2(u1, v1).color(color).next()
                );

            }
            mesh.render(stack);
        }
        GLYPH_PAGE_CACHE.clear();
        stack.pop();
        return getStringWidth(s, scale);
    }

    public double getStringWidth(String text, double scale) {
        char[] c = stripControlCodes(text).toCharArray();
        double currentLine = 0;
        double maxPreviousLines = 0;
        for (char c1 : c) {
            if (c1 == '\n') {
                maxPreviousLines = Math.max(currentLine, maxPreviousLines);
                currentLine = 0;
                continue;
            }
            Glyph glyph = locateGlyph1(c1);

            double gWidth = glyph == null ? 1 : glyph.width();

            currentLine += gWidth * scale;
        }
        return Math.max(currentLine, maxPreviousLines);
    }

    public double getWidth(String s, double scale) {
        return getStringWidth(s, scale);
    }

    public double getStringHeight(String text) {
        char[] c = stripControlCodes(text).toCharArray();
        if (c.length == 0) {
            c = new char[]{' '};
        }
        double currentLine = 0;
        double previous = 0;
        for (char c1 : c) {
            if (c1 == '\n') {
                if (currentLine == 0) {
                    // empty line, assume space
                    currentLine = locateGlyph1(' ').height();
                }
                previous += currentLine;
                currentLine = 0;
                continue;
            }
            Glyph glyph = locateGlyph1(c1);
            currentLine = Math.max(glyph.height(), currentLine);
        }
        return currentLine + previous;
    }

    @Override
    public void close() {
        for (GlyphMap map : maps) {
            map.destroy();
        }
        maps.clear();
        allGlyphs.clear();
    }

    public static int getGuiScale() {
        return (int) mc.getWindow().getScaleFactor();
    }

    @Contract(value = "_ -> new", pure = true)
    public static int[] RGBIntToRGB(int in) {
        int red = in >> 8 * 2 & 0xFF;
        int green = in >> 8 & 0xFF;
        int blue = in & 0xFF;
        return new int[]{red, green, blue};
    }

    @Contract(value = "-> new", pure = true)
    public static Identifier randomIdentifier() {
        return new Identifier("lemon-client", "temp/" + randomString(32));
    }

    private static String randomString(int length) {
        return IntStream.range(0, length)
            .mapToObj(operand -> String.valueOf((char) RND.nextInt(RND_START, RND_END + 1)))
            .collect(Collectors.joining());
    }

    record DrawEntry(double atX, double atY, float r, float g, float b, Glyph toDraw) {
    }
}
