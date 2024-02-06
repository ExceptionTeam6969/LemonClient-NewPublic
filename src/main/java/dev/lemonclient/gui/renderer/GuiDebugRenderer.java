package dev.lemonclient.gui.renderer;

import dev.lemonclient.enums.DrawMode;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WContainer;
import dev.lemonclient.renderer.Mesh;
import dev.lemonclient.renderer.ShaderMesh;
import dev.lemonclient.renderer.Shaders;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;

public class GuiDebugRenderer {
    private static final Color CELL_COLOR = new Color(25, 225, 25);
    private static final Color WIDGET_COLOR = new Color(25, 25, 225);

    private final Mesh mesh = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Lines, Mesh.Attrib.Vec2, Mesh.Attrib.Color);

    public void render(WWidget widget, MatrixStack matrices) {
        if (widget == null) return;

        mesh.begin();

        renderWidget(widget);

        mesh.end();
        mesh.render(matrices, 1.0f);
    }

    private void renderWidget(WWidget widget) {
        lineBox(widget.x, widget.y, widget.width, widget.height, WIDGET_COLOR);

        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer) widget).cells) {
                lineBox(cell.x, cell.y, cell.width, cell.height, CELL_COLOR);
                renderWidget(cell.widget());
            }
        }
    }

    private void lineBox(double x, double y, double width, double height, Color color) {
        line(x, y, x + width, y, color);
        line(x + width, y, x + width, y + height, color);
        line(x, y, x, y + height, color);
        line(x, y + height, x + width, y + height, color);
    }

    private void line(double x1, double y1, double x2, double y2, Color color) {
        mesh.line(
            mesh.vec2(x1, y1).color(color).next(),
            mesh.vec2(x2, y2).color(color).next()
        );
    }
}
