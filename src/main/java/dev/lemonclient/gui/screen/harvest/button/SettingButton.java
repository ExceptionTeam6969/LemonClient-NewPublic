package dev.lemonclient.gui.screen.harvest.button;
/*
import dev.lemonclient.addon.gui.screen.harvest.Component;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class SettingButton extends Component {
    public final MinecraftClient mc = MinecraftClient.getInstance();
    private Module module;
    private int x, y, w, h;

    protected final Color WHITE = new Color(255, 255, 255, 255);
    protected final Color GRAY = new Color(155, 155, 155, 255);
    protected final Color BLACK = new Color(20, 20, 20, 255);

    public SettingButton(Module module, int x, int y, int w, int h) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public void render(int mouseX, int mouseY) {
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
    }

    @Override
    public void mouseUp(int mouseX, int mouseY) {
    }

    @Override
    public void keyPress(int key) {
    }

    @Override
    public void close() {
    }

    public void drawButton() {
        int HUD = new Color(255,255,255,200).getPacked();
        MatrixStack stack = renderer.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        // Background
        renderer.drawRect(x() + 3, y(), w() - 3,  h(), BLACK);
        // Sides
        renderer.drawRect(x() + 3, y(),  2,  h(), HUD);
        renderer.drawRect(x() + w() - 3, y(),  w() - 2,  h(), HUD);
    }

    public void drawButton(int mouseX, int mouseY) {
        boolean hover = hover(x(), y(), w(), h() - 1, mouseX, mouseY);
        drawFlat(x() + 3, y() + 1, x() + w() - 3, y() + h(), hover ? Color.DARK_GRAY : Color.WHITE.a(200));
    }

    public void drawDescription(int mouseX, int mouseY, String settingDescription, SettingButton clazz) {
        if (!hover(x(), y(), w(), h() - 1, mouseX, mouseY)) return;

        String description = settingDescription == null ? "A Setting. (" + clazz.getClass().getSimpleName().replace("Setting", "") + ")" : settingDescription;

        int x = (int) (mc.getWindow().getScaledWidth() - vanillaText.width(description));
        int y = mc.getWindow().getScaledHeight() - 10;

        renderer.drawRect(x - 2, y - 2, (int) (vanillaText.width(description) + 1 * 1.5D), mc.textRenderer.fontHeight + 2, new Color(50, 50, 50).getPacked());
        vanillaText.drawWShadow(description, x, y, -1);
    }

    public int height() {
        return h;
    }
    public Module module() {
        return this.module;
    }
    public void module(Module module) {
        this.module = module;
    }
    public boolean drawn() {
        return this.module().drawn;
    }
    public void drawn(boolean drawn) {
        this.module().drawn = drawn;
    }

    public int x() {return x;}
    public void x(int x) {
        this.x = x;}
    public int y() {return y;}
    public void y(int y) {
        this.y = y;
    }
    public int w() {return w;}
    public void w(int w) {
        this.w = w;
    }
    public int h() {return h;}
    public void h(int h) {
        this.h = h;
    }

    public boolean hover(int X, int Y, int W, int H, int mouseX, int mouseY) {
        return mouseX >= X * 1.0F && mouseX <= (X + W) * 1.0F && mouseY >= Y * 1.0F && mouseY <= (Y + H) * 1.0F;
    }
}
*/
