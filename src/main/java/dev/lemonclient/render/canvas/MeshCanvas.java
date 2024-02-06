package dev.lemonclient.render.canvas;

import dev.lemonclient.render.CanvasManager;
import dev.lemonclient.render.helper.RenderHelper;
import dev.lemonclient.render.utils.BufferTexture;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeshCanvas extends CanvasRender implements RenderHelper {
    public static MeshCanvas CURRENT;

    private final Renderer2D renderer = CanvasManager.getRenderThreadR2d();
    private final Renderer2D texRenderer = CanvasManager.getRenderThreadTexR2d();

    private final List<Runnable> msaaRenderCall = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    private boolean building;

    private MeshCanvas() {
    }

    public static MeshCanvas newCanvas() {
        MeshCanvas canvas = new MeshCanvas();
        CanvasManager.canvas.add(canvas);
        CURRENT = canvas;
        return canvas;
    }

    @Override
    public void begin() {
        if (building && !isImmutableMode()) return;

        building = true;

        msaaRenderCall.clear();
        Runnable task = () -> {
            renderer.begin();
            texRenderer.begin();
        };

        if (isMsaa()) {
            addTask(task);
            return;
        }

        task.run();
    }

    @Override
    public void drawRect(float x, float y, float width, float height, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            renderer.quad(x, y, width, height, color);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawRoundRect(float x, float y, float width, float height, float radius, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            Renderer2D.COLOR.quadRounded(x, y, width, height, radius, color);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawRoundRectLine(float x, float y, float width, float height, float radius, float lineWidth, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            Renderer2D.COLOR.quadRoundedOutline(x, y, width, height, color, radius, lineWidth);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawRoundRectOutline(float x, float y, float width, float height, float radius, float outlineWidth, Color inColor, Color outColor) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            float temp = outlineWidth;

            if (outlineWidth >= radius) {
                temp = outlineWidth / 2;
            }

            Renderer2D.COLOR.quadRoundedOutline(x, y, width, height, outColor, radius, temp);

            float rX = x + temp;
            float rY = x + temp;
            float rW = width - temp * 2;
            float rH = height - temp * 2;

            Renderer2D.COLOR.quadRounded(rX, rY, rW, rH, radius, inColor);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            renderer.line(x1, y1, x2, y2, color);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawCircle(float x, float y, float radius, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            Renderer2D.COLOR.circlePart(x, y, radius, circleNone, circleAll, color);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    @Override
    public void drawTexture(Identifier id, double x, double y, double width, double height, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();
            GL.bindTexture(id);
            texRenderer.quad(x, y, width, height, color);
            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    private final IntBuffer data_buffer = GlAllocationUtils.allocateByteBuffer(16777216).asIntBuffer();

    @Override
    public BufferTexture setupImage(BufferedImage image) {
        int iw = image.getWidth();
        int ih = image.getHeight();
        int[] data = new int[iw * ih];
        image.getRGB(0, 0, iw, ih, data, 0, iw);
        return new BufferTexture(iw, ih, data, BufferTexture.Format.RGBA, BufferTexture.Filter.Linear, BufferTexture.Filter.Linear);
    }

    @Override
    public void drawTexture(NativeImage id, double x, double y, double width, double height, Color color) {
        this.drawTexture(new NativeImageBackedTexture(id), x, y, width, height, color);
    }

    @Override
    public void drawTexture(NativeImageBackedTexture id, double x, double y, double width, double height, Color color) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();
            GL.bindTexture(id.getGlId());
            texRenderer.quad(x, y, width, height, color);
            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }


    @Override
    public void render(MatrixStack matrix) {
        if (!building && !isImmutableMode()) return;

        building = false;
        Runnable task = () -> {
            renderer.render(matrix);
            texRenderer.render(matrix);
        };
        if (isMsaa()) {
            addTask(task);
            MSAAFramebuffer.use(this::execCalls);
            return;
        }

        task.run();
        msaaRenderCall.clear();
    }

    public void drawArcOutline(float x, float y, float radius, float thickness, float startAngleIn, float endAngleIn, float startAngleOut, float endAngleOut, Color inColor, Color outColor) {
        Runnable task = () -> {
            if (isImmutableMode()) begin();

            Renderer2D.COLOR.circlePart(x, y, radius, startAngleIn, endAngleIn, inColor);
            float oR = radius + thickness;

            Renderer2D.COLOR.circlePartOutline(x, y, oR, startAngleOut, endAngleOut, outColor, thickness);

            if (isImmutableMode()) render(getMatrixStack());
        };

        boolean immutable = isImmutableMode();
        if (isMsaa()) {
            if (immutable) {
                disableImmutableMode();
                begin();
            }

            addTask(task);

            if (immutable) {
                render(getMatrixStack());
                enableImmutableMode();
            }
            return;
        }

        task.run();
    }

    private void execCalls() {
        for (Runnable runnable : msaaRenderCall) {
            runnable.run();
        }
    }

    @Override
    public void end() {
        Runnable task = () -> {
            renderer.end();
            texRenderer.end();
        };

        if (isMsaa()) {
            addTask(task);
            return;
        }

        task.run();
    }

    private void addTask(Runnable runnable) {
        msaaRenderCall.add(runnable);
    }
}
