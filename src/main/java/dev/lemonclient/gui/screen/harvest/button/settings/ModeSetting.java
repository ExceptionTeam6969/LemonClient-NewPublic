package dev.lemonclient.gui.screen.harvest.button.settings;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class ModeSetting extends SettingButton {
    private final EnumSetting setting;

    public ModeSetting(Module module, EnumSetting setting, int x, int y, int w, int h) {
        super(module, x, y, w, h);
        this.setting = setting;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        drawButton();
        drawDescription(mouseX, mouseY, this.setting.description, this);

        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        drawButton(mouseX, mouseY);
        vanillaText.drawWShadow(setting.name, (float) (x() + 6), (float) (y() + 4), WHITE);
        vanillaText.drawWShadow(setting.get().toString(), (float) ((x() + 6) + vanillaText.width(" " + setting.name)), (float) (y() + 4), GRAY);
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (hover(x(), y(), w(), h() - 1, mouseX, mouseY)) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            if (button == 0) {
                int i = 0;
                int enumIndex = 0;
                for (Object enumName : setting.getSuggestions()) {
                    if (enumName.equals(setting.get()))
                        enumIndex = i;
                    i++;
                }
                if (enumIndex == setting.getSuggestions().size() - 1) {
                    String mode = (String) setting.getSuggestions().get(0);
                    setting.set(mode);
                } else {
                    enumIndex++;
                    i = 0;
                    for (Object enumName : setting.getSuggestions()) {
                        if (i == enumIndex) {
                            setting.set(enumName);
                        }
                        i++;
                    }
                }
            } else if (button == 1) {
                int i = 0;
                int enumIndex = 0;
                for (Object enumName : setting.getSuggestions()) {
                    if (enumName.equals(setting.get()))
                        enumIndex = i;
                    i++;
                }
                if (enumIndex == 0) {
                    Object mode = setting.getSuggestions().get(setting.getSuggestions().size() - 1);
                    setting.set(mode);
                } else {
                    enumIndex--;
                    i = 0;
                    for (Object enumName : setting.getSuggestions()) {
                        if (i == enumIndex) {
                            setting.set(enumName);
                        }
                        i++;
                    }
                }
            }
        }
    }
}*/
