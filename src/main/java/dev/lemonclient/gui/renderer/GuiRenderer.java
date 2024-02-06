package dev.lemonclient.gui.renderer;

import dev.lemonclient.LemonClient;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.renderer.operations.TextOperation;
import dev.lemonclient.gui.renderer.packer.GuiTexture;
import dev.lemonclient.gui.renderer.packer.TexturePacker;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.Texture;
import dev.lemonclient.utils.PostInit;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.Pool;
import dev.lemonclient.utils.render.ByteTexture;
import dev.lemonclient.utils.render.RenderUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GuiRenderer {
    private static final Color WHITE = new Color(255, 255, 255);

    private static final TexturePacker TEXTURE_PACKER = new TexturePacker();
    private static ByteTexture TEXTURE;

    public static GuiTexture CIRCLE;
    public static GuiTexture TRIANGLE;
    public static GuiTexture EDIT;
    public static GuiTexture RESET;
    public static GuiTexture FAVORITE_NO, FAVORITE_YES;

    public GuiTheme theme;

    private final Renderer2D r = new Renderer2D(false);
    private final Renderer2D rTex = new Renderer2D(true);

    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new Stack<>();

    private final Pool<TextOperation> textPool = new Pool<>(TextOperation::new);
    private final List<TextOperation> texts = new ArrayList<>();

    private final List<Runnable> postTasks = new ArrayList<>();

    public String tooltip, lastTooltip;
    public WWidget tooltipWidget;
    private double tooltipAnimProgress;

    private DrawContext drawContext;

    public static GuiTexture addTexture(Identifier id) {
        return TEXTURE_PACKER.add(id);
    }

    @PostInit
    public static void init() {
        CIRCLE = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/circle.png"));
        TRIANGLE = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/triangle.png"));
        EDIT = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/edit.png"));
        RESET = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/reset.png"));
        FAVORITE_NO = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/favorite_no.png"));
        FAVORITE_YES = addTexture(new Identifier(LemonClient.MOD_ID, "textures/icons/gui/favorite_yes.png"));

        TEXTURE = TEXTURE_PACKER.pack();
    }

    public void begin(DrawContext drawContext) {
        this.drawContext = drawContext;

        GL.enableBlend();
        GL.enableScissorTest();
        scissorStart(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight());
    }

    public void end() {
        scissorEnd();

        for (Runnable task : postTasks) task.run();
        postTasks.clear();

        GL.disableScissorTest();
    }

    public void beginRender() {
        r.begin();
        rTex.begin();
    }

    public void endRender() {
        r.end();
        rTex.end();

        r.render(drawContext.getMatrices());

        GL.bindTexture(TEXTURE.getGlId());
        rTex.render(drawContext.getMatrices());

        // Normal text
        theme.textRenderer().begin(theme.scale(1));
        for (TextOperation text : texts) {
            if (!text.title) text.run(textPool);
        }
        theme.textRenderer().end(drawContext.getMatrices());

        // Title text
        theme.textRenderer().begin(theme.scale(1.25));
        for (TextOperation text : texts) {
            if (text.title) text.run(textPool);
        }
        theme.textRenderer().end(drawContext.getMatrices());

        texts.clear();
    }

    public void scissorStart(double x, double y, double width, double height) {
        if (!scissorStack.isEmpty()) {
            Scissor parent = scissorStack.peek();

            if (x < parent.x) x = parent.x;
            else if (x + width > parent.x + parent.width) width -= (x + width) - (parent.x + parent.width);

            if (y < parent.y) y = parent.y;
            else if (y + height > parent.y + parent.height) height -= (y + height) - (parent.y + parent.height);

            parent.apply();
            endRender();
        }

        scissorStack.push(scissorPool.get().set(x, y, width, height));
        beginRender();
    }

    public void scissorEnd() {
        Scissor scissor = scissorStack.pop();

        scissor.apply();
        endRender();
        for (Runnable task : scissor.postTasks) task.run();
        if (!scissorStack.isEmpty()) beginRender();

        scissorPool.free(scissor);
    }

    public boolean renderTooltip(DrawContext drawContext, double mouseX, double mouseY, double delta) {
        tooltipAnimProgress += (tooltip != null ? 1 : -1) * delta * 14;
        tooltipAnimProgress = MathHelper.clamp(tooltipAnimProgress, 0, 1);

        boolean toReturn = false;

        if (tooltipAnimProgress > 0) {
            if (tooltip != null && !tooltip.equals(lastTooltip)) {
                tooltipWidget = theme.tooltip(tooltip);
                tooltipWidget.init();
            }

            tooltipWidget.move(-tooltipWidget.x + mouseX + 12, -tooltipWidget.y + mouseY + 12);

            setAlpha(tooltipAnimProgress);

            begin(drawContext);
            tooltipWidget.render(this, mouseX, mouseY, delta);
            end();

            setAlpha(1);

            lastTooltip = tooltip;
            toReturn = true;
        }

        tooltip = null;
        return toReturn;
    }

    public void setAlpha(double a) {
        r.setAlpha(a);
        rTex.setAlpha(a);

        theme.textRenderer().setAlpha(a);
    }

    public void tooltip(String text) {
        tooltip = text;
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        r.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void quad(double x, double y, double width, double height, Color colorLeft, Color colorRight) {
        quad(x, y, width, height, colorLeft, colorRight, colorRight, colorLeft);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color);
    }

    public void quad(WWidget widget, Color color) {
        quad(widget.x, widget.y, widget.width, widget.height, color);
    }

    public void quad(double x, double y, double width, double height, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, texture.get(width, height), color);
    }

    public void rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, rotation, texture.get(width, height), color);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        r.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void text(String text, double x, double y, Color color, boolean title) {
        texts.add(getOp(textPool, x, y, color).set(text, theme.textRenderer(), title));
    }

    public void quadRounded(double x, double y, double width, double height, Color color, double round, boolean roundTop) {
        r.quadRounded(x, y, width, height, color, round, roundTop);
        //Render2DUtils.drawGradientGlow(RenderSystem.getModelViewStack(), color, color, color, color, (float) x, (float) y, (float) width, (float) height, (float) round, (float) round);
    }

    public void quadRounded(double x, double y, double width, double height, Color color, double round) {
        quadRounded(x, y, width, height, color, round, true);
    }

    public void quadRounded(WWidget widget, Color color, double round) {
        quadRounded(widget.x, widget.y, widget.width, widget.height, color, round);
    }

    public void quadOutlineRounded(double x, double y, double width, double height, Color color, double round, double s) {
        r.quadRoundedOutline(x, y, width, height, color, round, s);
    }

    public void quadOutlineRounded(WWidget widget, Color color, double round, double s) {
        quadOutlineRounded(widget.x, widget.y, widget.width, widget.height, color, round, s);
    }

    public void quadRoundedSide(double x, double y, double width, double height, Color color, double r, boolean right) {
        this.r.quadRoundedSide(x, y, width, height, color, r, right);
    }

    public void quadRoundedSide(WWidget widget, Color color, double round, boolean right) {
        quadRoundedSide(widget.x, widget.y, widget.width, widget.height, color, round, right);
    }

    public void circlePart(double x, double y, double r, double startAngle, double angle, Color color) {
        this.r.circlePart(x, y, r, startAngle, angle, color);
    }

    public void circlePartOutline(double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        this.r.circlePartOutline(x, y, r, startAngle, angle, color, outlineWidth);
    }

    public void texture(double x, double y, double width, double height, double rotation, Texture texture) {
        post(() -> {
            rTex.begin();
            rTex.texQuad(x, y, width, height, rotation, 0, 0, 1, 1, WHITE);
            rTex.end();

            texture.bind();
            rTex.render(drawContext.getMatrices());
        });
    }

    public void post(Runnable task) {
        scissorStack.peek().postTasks.add(task);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(drawContext, itemStack, x, y, scale, overlay);
    }

    public void absolutePost(Runnable task) {
        postTasks.add(task);
    }

    private <T extends GuiRenderOperation<T>> T getOp(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }
}
