package dev.lemonclient.gui.screen.harvest.button.settings;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.Keybind;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class BindSetting extends SettingButton {
    private final Module module;
    private boolean binding;

    public BindSetting(Module module, int x, int y, int w, int h) {
        super(module, x, y, w, h);
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        drawButton();
        drawButton(mouseX, mouseY);

        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        int color = new Color(255,255,255,200).getPacked();
        renderer.drawLine(x(), y(), w(), h(), color);

        vanillaText.drawWShadow("Bind", (float) (x() + 6), (float) (y() + 4), WHITE);
        vanillaText.drawWShadow(binding ? "..." : Utils.getKeyName(module.keybind.getValue()), (float) ((x() + 6) + vanillaText.width(" Bind")), (float) (y() + 4), GRAY);
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (hover(x(), y(), w(), h() - 1, mouseX, mouseY)) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            binding = !binding;
        }
    }

    @Override
    public void keyPress(int key) {
        if (binding) {
            if (key == 256 || key == 259 || key == 261) {
                this.module().keybind.set(Keybind.fromKey(GLFW.GLFW_KEY_UNKNOWN));
            } else module().keybind.set(Keybind.fromKey(key));

            binding = false;
        }
    }
}*/
