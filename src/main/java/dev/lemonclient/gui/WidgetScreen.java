package dev.lemonclient.gui;

import dev.lemonclient.LemonClient;
import dev.lemonclient.gui.renderer.GuiDebugRenderer;
import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.tabs.TabScreen;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WRoot;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WContainer;
import dev.lemonclient.gui.widgets.input.WTextBox;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.CursorStyle;
import dev.lemonclient.utils.misc.input.Input;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.gui.MeteorRenderer;
import dev.lemonclient.utils.render.gui.NewParticleRenderer;
import dev.lemonclient.utils.render.gui.ParticleRenderer;
import dev.lemonclient.utils.render.gui.SnowRenderer;
import dev.lemonclient.utils.timers.MSTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.lemonclient.LemonClient.mc;
import static org.lwjgl.glfw.GLFW.*;

public abstract class WidgetScreen extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();

    public Runnable taskAfterRender;
    protected Runnable enterAction;

    public Screen parent;
    private final WContainer root;

    protected final GuiTheme theme;

    public boolean locked, lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;

    private double lastMouseX, lastMouseY;

    public double animProgress;

    private List<Runnable> onClosed;

    protected boolean firstInit = true;

    private final MSTimer timer = new MSTimer();

    private MeteorRenderer meteorSystem = new MeteorRenderer(30);
    private SnowRenderer snowRenderer = new SnowRenderer();
    private NewParticleRenderer particleRenderer = new NewParticleRenderer();
    private ParticleRenderer particleRenderer0 = new ParticleRenderer(300);

    public WidgetScreen(GuiTheme theme, String title) {
        super(Text.literal(title));

        this.parent = mc.currentScreen;
        this.root = new WFullScreenRoot();
        this.theme = theme;

        root.theme = theme;

        if (parent != null) {
            animProgress = 1;

            if (this instanceof TabScreen && parent instanceof TabScreen) {
                parent = ((TabScreen) parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return root.add(widget);
    }

    public void clear() {
        root.clear();
    }

    public void invalidate() {
        root.invalidate();
    }

    @Override
    protected void init() {
        LemonClient.EVENT_BUS.subscribe(this);

        closed = false;

        if (firstInit) {
            firstInit = false;
            initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        clear();
        initWidgets();
    }

    public void onClosed(Runnable action) {
        if (onClosed == null) onClosed = new ArrayList<>(2);
        onClosed.add(action);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (locked) return false;

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        return root.mouseClicked(mouseX, mouseY, button, false);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (locked) return false;

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        return root.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (locked) return;

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (locked) return false;

        root.mouseScrolled(amount);

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (locked) return false;

        if ((modifiers == GLFW_MOD_CONTROL || modifiers == GLFW_MOD_SUPER) && keyCode == GLFW_KEY_9) {
            debug = !debug;
            return true;
        }

        if ((keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER) && enterAction != null) {
            enterAction.run();
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (locked) return false;

        boolean shouldReturn = root.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        if (shouldReturn) return true;

        // Select next text box if TAB was pressed
        if (keyCode == GLFW_KEY_TAB) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);

            loopWidgets(root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox textBox)) return;

                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();

                    done.set(true);
                } else {
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                        foundFocused.set(true);
                    }
                }

                if (firstTextBox.get() == null) firstTextBox.set(textBox);
            });

            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
            }

            return true;
        }

        boolean control = MinecraftClient.IS_SYSTEM_MAC ? modifiers == GLFW_MOD_SUPER : modifiers == GLFW_MOD_CONTROL;

        if (control && keyCode == GLFW_KEY_C && toClipboard()) {
            return true;
        } else if (control && keyCode == GLFW_KEY_V && fromClipboard()) {
            reload();
            if (parent instanceof WidgetScreen wScreen) {
                wScreen.reload();
            }
            return true;
        }

        return false;
    }

    public void keyRepeated(int key, int modifiers) {
        if (locked) return;

        root.keyRepeated(key, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (locked) return false;

        return root.charTyped(chr);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) renderBackground(context);

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        animProgress += delta / 20 * 14;
        animProgress = MathHelper.clamp(animProgress, 0, 1);

        GuiKeyEvents.canUseKeys = true;

        // Apply projection without scaling
        Utils.unscaledProjection();

        onRenderBefore(context, delta);

        RENDERER.theme = theme;
        theme.beforeRender();

        int finalMouseX = mouseX;
        int finalMouseY = mouseY;
        Runnable task = () -> {
            if (theme instanceof LCGuiTheme t) {
                if (t.lineMeteor.get()) {
                    meteorSystem.setRainbow(t.meteorRainbow.get());
                    meteorSystem.tick();
                    meteorSystem.render(context);
                }

                if (t.snowParticles.get()) {
                    snowRenderer.tick(t.spawnDelays.get(), context.getScaledWindowHeight());
                    snowRenderer.render(context);
                }

                if (t.particles.get()) {
                    if (t.bothParticles.get()) {
                        Renderer2D.COLOR.begin();
                        if (timer.hasTimePassed(50)) {
                            particleRenderer.update();
                            timer.reset();
                        }
                        particleRenderer.render(finalMouseX, finalMouseY);
                        Renderer2D.COLOR.render(context.getMatrices());
                    }

                    particleRenderer0.render(context, mc.getWindow().getWidth(), mc.getWindow().getHeight());
                }
            }

            RENDERER.begin(context);
            RENDERER.setAlpha(animProgress);
            root.render(RENDERER, finalMouseX, finalMouseY, delta / 20);
            RENDERER.setAlpha(1);
            RENDERER.end();

            boolean tooltip = RENDERER.renderTooltip(context, finalMouseX, finalMouseY, delta / 20);
            if (debug) {
                MatrixStack matrices = context.getMatrices();

                DEBUG_RENDERER.render(root, matrices);
                if (tooltip) DEBUG_RENDERER.render(RENDERER.tooltipWidget, matrices);
            }
        };

        if (GuiThemes.get() instanceof LCGuiTheme) {
            MSAAFramebuffer.use(task);
        } else {
            task.run();
        }

        Utils.scaledProjection();
        runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (taskAfterRender != null) {
            taskAfterRender.run();
            taskAfterRender = null;
        }
    }

    protected void onRenderBefore(DrawContext drawContext, float delta) {
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        root.invalidate();

        meteorSystem = new MeteorRenderer(30);
        snowRenderer = new SnowRenderer();
        particleRenderer = new NewParticleRenderer();
        particleRenderer0 = new ParticleRenderer(300);
    }

    @Override
    public void close() {
        if (!locked || lockedAllowClose) {
            boolean preOnClose = onClose;
            onClose = true;

            removed();

            onClose = preOnClose;
        }
    }

    @Override
    public void removed() {
        if (!closed || lockedAllowClose) {
            closed = true;
            onClosed();

            Input.setCursorStyle(CursorStyle.Default);

            loopWidgets(root, widget -> {
                if (widget instanceof WTextBox textBox && textBox.isFocused()) textBox.setFocused(false);
            });

            LemonClient.EVENT_BUS.unsubscribe(this);
            GuiKeyEvents.canUseKeys = true;

            if (onClosed != null) {
                for (Runnable action : onClosed) action.run();
            }

            if (onClose) {
                taskAfterRender = () -> {
                    locked = true;
                    mc.setScreen(parent);
                };
            }
        }
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);

        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer) widget).cells) loopWidgets(cell.widget(), action);
        }
    }

    protected void onClosed() {
    }

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !locked || lockedAllowClose;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class WFullScreenRoot extends WContainer implements WRoot {
        private boolean valid;

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        protected void onCalculateSize() {
            width = Utils.getWindowWidth();
            height = Utils.getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell<?> cell : cells) {
                cell.x = 0;
                cell.y = 0;

                cell.width = width;
                cell.height = height;

                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!valid) {
                calculateSize();
                calculateWidgetPositions();

                valid = true;
                mouseMoved(mc.mouse.getX(), mc.mouse.getY(), mc.mouse.getX(), mc.mouse.getY());
            }

            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
