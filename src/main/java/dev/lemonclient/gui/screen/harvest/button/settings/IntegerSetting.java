package dev.lemonclient.gui.screen.harvest.button.settings;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class IntegerSetting extends SettingButton {
    private final IntSetting setting;
    protected boolean dragging;
    protected double sliderWidth;

    public IntegerSetting(Module module, IntSetting setting, int x, int y, int w, int h) {
        super(module, x, y, w, h);
        this.dragging = false;
        this.sliderWidth = 0;
        this.setting = setting;
    }

    protected void updateSlider(int mouseX) {
    }

    @Override
    public void render(int mouseX, int mouseY) {
        updateSlider(mouseX);

        drawButton();
        drawDescription(mouseX, mouseY, this.setting.description, this);

        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        boolean hover = hover(x(), y(), w(), h() - 1, mouseX, mouseY);
        drawBackground(true);
        drawFlat(x() + 3, y() + 14, (int) (x() - 2 + (sliderWidth) + 5), y() + h(), hover ? Color.DARK_GRAY : Color.WHITE.a(200));

        vanillaText.drawWShadow(setting.name, (float) (x() + 6), (float) (y() + 4), WHITE);
        vanillaText.drawWShadow(String.valueOf(setting.get()), (float) ((x() + 6) + vanillaText.width(" " + setting.name)), (float) (y() + 4), WHITE);
    }

    private void drawBackground(boolean background) {
        if (background) {
            drawFlat(x() + 3, y() + 1, x() + w() - 3, y() + h(), BLACK);
        } else {
            drawFlat((int) (x() + 3 + (sliderWidth)), y() + 14, x() + w() - 3, y() + h(), BLACK);
        }
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (button == 1) {
            dragging = false;
            return;
        }

        if (hover(x(), y(), w(), h() - 1, mouseX, mouseY)) {
            if (!dragging) mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            dragging = true;
        }
    }

    @Override
    public void mouseUp(int mouseX, int mouseY) {
        dragging = false;
    }

    @Override
    public void close() {
        dragging = false;
    }

    public static class Slider extends IntegerSetting {
        private final IntSetting setting;

        public Slider(Module module, IntSetting setting, int x, int y, int w, int h) {
            super(module, setting, x, y, w, h);
            this.setting = setting;
        }

        @Override
        protected void updateSlider(int mouseX) {
            final double diff = Math.min(w(), Math.max(0, mouseX - x()));
            final double min = setting.sliderMin;
            final double max = setting.sliderMax;
            sliderWidth = (w() - 6) * (setting.get() - min) / (max - min);

            if (dragging) {
                setting.set(diff == 0.0 ? setting.min : (int) Double.parseDouble(String.format("%." + 1 + "f", diff / w() * (max - min) + min).replace(",", ".")));
            }
        }
    }
}
*/
