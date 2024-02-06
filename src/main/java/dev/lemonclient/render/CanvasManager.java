package dev.lemonclient.render;

import dev.lemonclient.enums.DrawMode;
import dev.lemonclient.render.canvas.*;
import dev.lemonclient.renderer.*;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CanvasManager {
    private static Renderer2D renderThreadR2d;
    private static Renderer2D renderThreadTexR2d;
    private static Renderer3D renderThreadR3d;
    private static Mesh renderThreadMesh2d;
    private static Mesh renderThreadMeshTex2d;
    private static CanvasRender renderThreadCanvas;
    private static TextCanvas vanillaTextCanvas;
    private static TextCanvas betterTextCanvas;

    public static List<MatrixCanvas> canvas = new CopyOnWriteArrayList<>();

    public static void init() {
        renderThreadR2d = Renderer2D.COLOR;
        renderThreadTexR2d = Renderer2D.TEXTURE;
        renderThreadR3d = new Renderer3D();
        renderThreadMesh2d = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
        renderThreadMeshTex2d = new ShaderMesh(Shaders.POS_TEX_COLOR, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
        renderThreadCanvas = MeshCanvas.newCanvas();
        vanillaTextCanvas = VanillaTextCanvas.newCanvas();
        betterTextCanvas = BetterTextCanvas.newCanvas();
    }

    public static void loopCanvas(DrawContext context) {
        canvas.forEach(c -> {
            try {
                c.setDrawContext(context);
            } catch (Exception ignored) {
            }
        });
    }

    public static TextCanvas getVanillaTextCanvas() {
        return vanillaTextCanvas;
    }

    public static void setVanillaTextCanvas(TextCanvas vanillaTextCanvas) {
        CanvasManager.vanillaTextCanvas = vanillaTextCanvas;
    }

    public static TextCanvas getBetterTextCanvas() {
        return betterTextCanvas;
    }

    public static void setBetterTextCanvas(TextCanvas betterTextCanvas) {
        CanvasManager.betterTextCanvas = betterTextCanvas;
    }

    public static Renderer3D getRenderThreadR3d() {
        return renderThreadR3d;
    }

    public static void setRenderThreadR3d(Renderer3D renderThreadR3d) {
        CanvasManager.renderThreadR3d = renderThreadR3d;
    }

    public static CanvasRender getRenderThreadCanvas() {
        return renderThreadCanvas;
    }

    public static void setRenderThreadCanvas(CanvasRender renderThreadCanvas) {
        CanvasManager.renderThreadCanvas = renderThreadCanvas;
    }

    public static Mesh getRenderThreadMeshTex2d() {
        return renderThreadMeshTex2d;
    }

    public static void setRenderThreadMeshTex2d(Mesh renderThreadMeshTex2d) {
        CanvasManager.renderThreadMeshTex2d = renderThreadMeshTex2d;
    }

    public static Renderer2D getRenderThreadR2d() {
        return renderThreadR2d;
    }

    public static void setRenderThreadR2d(Renderer2D renderThreadR2d) {
        CanvasManager.renderThreadR2d = renderThreadR2d;
    }

    public static Renderer2D getRenderThreadTexR2d() {
        return renderThreadTexR2d;
    }

    public static void setRenderThreadTexR2d(Renderer2D renderThreadTexR2d) {
        CanvasManager.renderThreadTexR2d = renderThreadTexR2d;
    }

    public static Mesh getRenderThreadMesh2d() {
        return renderThreadMesh2d;
    }

    public static void setRenderThreadMesh2d(Mesh renderThreadMesh2d) {
        CanvasManager.renderThreadMesh2d = renderThreadMesh2d;
    }
}
