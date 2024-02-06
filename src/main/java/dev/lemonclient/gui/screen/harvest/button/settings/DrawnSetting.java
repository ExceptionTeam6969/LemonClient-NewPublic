package dev.lemonclient.gui.screen.harvest.button.settings;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class DrawnSetting extends SettingButton {
    public DrawnSetting(Module module, int x, int y, int w, int h) {
        super(module, x, y, w, h);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        drawButton();

        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        if (this.drawn()) {
            drawButton(mouseX, mouseY);
        } else {
            boolean hover = hover(x(), y(), w(), h() - 1, mouseX, mouseY);
            drawFlat(x() + 3, y() + 1, x() + w() - 3, y() + h(), hover ? new Color(new java.awt.Color(BLACK.getPacked()).brighter()) : BLACK);
        }

        vanillaText.drawWShadow("Drawn", (float) (x() + 6), (float) (y() + 4), this.module().drawn ? WHITE : GRAY);
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (hover(x(), y(), w(), h() - 1, mouseX, mouseY) && (button == 0 || button == 1)) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            this.drawn(!this.drawn());
        }
    }
}
*/
