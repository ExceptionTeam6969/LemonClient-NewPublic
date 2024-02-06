package dev.lemonclient.render.gui;

import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetContainer {
    public final List<Widget> widgets = new CopyOnWriteArrayList<>();

    public GuiRenderer renderer;

    protected final boolean withoutScale, msaa;

    public WidgetContainer() {
        this(null, true, false);
    }

    public WidgetContainer(GuiRenderer renderer, boolean withoutScale, boolean msaa) {
        this.renderer = renderer;
        this.withoutScale = withoutScale;
        this.msaa = msaa;
    }

    public Widget getWidget(int wId) {
        Optional<Widget> request = widgets.stream().filter((w) -> w.widgetId == wId).findFirst();
        return request.orElse(null);
    }

    public WidgetContainer addWidget(Widget widget) {
        widgets.add(widget);
        widget.widgetId = widgets.size();
        return this;
    }

    public void render(DrawContext context, double mouseX, double mouseY, float tickDelta) {
        assertRenderer(context);
        Runnable task = () -> {
            if (withoutScale) {
                Utils.unscaledProjection();
            }

            widgets.forEach(w -> w.preRender(renderer, mouseX, mouseY, tickDelta));

            if (withoutScale) {
                Utils.scaledProjection();
            }
        };

        if (msaa) MSAAFramebuffer.use(task);
        else task.run();
    }

    public void tick() {
        widgets.forEach(Widget::preTick);
    }

    public void mouseMove(double x, double y) {
        widgets.forEach((w) -> {
            w.preMouseMove(x, y);
        });
    }

    public void mouseClick(double x, double y, int button) {
        widgets.forEach((w) -> {
            w.preMouseClick(x, y, button);
        });
    }

    private void assertRenderer(DrawContext context) {
        if (renderer == null) {
            renderer = new GuiRenderer(context);
            renderer.mesh = true;
        }
    }
}
