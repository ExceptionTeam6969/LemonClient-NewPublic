package dev.lemonclient.renderer;

import dev.lemonclient.enums.DrawMode;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;

public class MeshRenderer {
    public final SharedShaderMesh triangles;
    public final SharedShaderMesh triangles_texture;
    public final SharedShaderMesh lines;

    public static MeshRenderer INSTANCE;
    private final long startMS;

    public MeshRenderer() {
        triangles = new SharedShaderMesh(Shaders.RECT, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
        triangles_texture = new SharedShaderMesh(Shaders.TEXTURE_RECT, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Vec2, Mesh.Attrib.Color);

        lines = new SharedShaderMesh(Shaders.POS_COLOR, DrawMode.Lines, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
        startMS = System.currentTimeMillis();
        INSTANCE = this;
    }

    private void setShader(Shader shader) {
        this.triangles.shader = shader;
    }

    private void setTexShader(Shader shader) {
        this.triangles_texture.shader = shader;
    }

    public void setAlpha(double alpha) {
        triangles.alpha = alpha;
    }

    public void begin() {
        triangles.begin();
        triangles_texture.begin();
        lines.begin();
    }

    public void end() {
        triangles.end();
        triangles_texture.end();
        lines.end();
    }

    public void render() {
        triangles.render(null, 1.0f);
        triangles_texture.render(null, 1.0f);
        lines.render(null, 1.0f);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        setShader(Shaders.RECT);
        triangles.triangle(
            triangles.vec2(x1, y1).color(color).next(),
            triangles.vec2(x2, y2).color(color).next(),
            triangles.vec2(x3, y3).color(color).next()
        );
    }

    public void line(double x1, double y1, double x2, double y2, Color color) {
        lines.line(
            lines.vec2(x1, y1).color(color).next(),
            lines.vec2(x2, y2).color(color).next()
        );
    }

    public void rect(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        setShader(Shaders.RECT);
        setupRect(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void rect(double x, double y, double width, double height, Color color) {
        this.rect(x, y, width, height, color, color, color, color);
    }

    public void texRect(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        setTexShader(Shaders.TEXTURE_RECT);
        triangles_texture.quad(
            triangles_texture.vec2(x, y).uv(0, 0).color(cTopLeft).next(),
            triangles_texture.vec2(x, y + height).uv(0, 1).color(cTopRight).next(),
            triangles_texture.vec2(x + width, y + height).uv(1, 1).color(cBottomLeft).next(),
            triangles_texture.vec2(x + width, y).uv(1, 0).color(cBottomRight).next()
        );
    }

    public void texRect(double x, double y, double width, double height, Color color) {
        this.texRect(x, y, width, height, color, color, color, color);
    }

    public void roundRect(double x, double y, double width, double height, double radius, Color color) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quadRounded(x, y, width, height, color, radius, true);
        Renderer2D.COLOR.render(null);
    }

    public void mainMenu(double x, double y, double width, double height, int type) {
        setShader(Shaders.MAIN_MENU);
        this.triangles.set("uSize", width * getFactor(), height * getFactor());
        this.triangles.set("Time", (System.currentTimeMillis() - startMS) / 1000F);
        this.triangles.set("Type", type);
        setupRect(x, y, width, height);
    }

    public void setupRect(double x, double y, double width, double height) {
        this.setupRect(x, y, width, height, Color.WHITE);
    }

    public void setupRect(double x, double y, double width, double height, Color color) {
        this.setupRect(x, y, width, height, color, color, color, color);
    }

    public void setupRect(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        triangles.quad(
            triangles.vec2(x, y).color(cTopLeft).next(),
            triangles.vec2(x, y + height).color(cBottomLeft).next(),
            triangles.vec2(x + width, y + height).color(cBottomRight).next(),
            triangles.vec2(x + width, y).color(cTopRight).next()
        );
    }

    private double getFactor() {
        return Utils.rendering3D ? MinecraftClient.getInstance().getWindow().getScaleFactor() : 1;
    }
}
