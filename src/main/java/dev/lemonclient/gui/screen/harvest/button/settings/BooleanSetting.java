package dev.lemonclient.gui.screen.harvest.button.settings;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class BooleanSetting extends SettingButton {
    private final BoolSetting setting;

    public BooleanSetting(Module module, BoolSetting setting, int x, int y, int w, int h) {
        super(module, x, y, w, h);
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        drawButton();
        drawDescription(mouseX, mouseY, this.setting.description, this);

        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        drawFlat(x() + 3, y() + 1, x() + w() - 3, y() + h(), BLACK);
        drawCheckBox(setting.get(), x() + w() - 13, y() + 3);

        vanillaText.drawWShadow(setting.title, (float) (x() + 6), (float) (y() + 4), setting.get() ? WHITE : GRAY);
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (hover(x(), y(), w(), h() - 1, mouseX, mouseY) && button == 0) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);

            setting.set(!setting.get());
        }
    }
}
*/
