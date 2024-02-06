package dev.lemonclient.gui.screen.harvest.button;
/*
import dev.lemonclient.addon.gui.screen.harvest.Component;
import dev.lemonclient.addon.gui.screen.harvest.button.settings.*;
import dev.lemonclient.addon.gui.screen.harvest.button.settings.DoubleSetting;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModuleButton extends Component {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Module module;
    private final List<SettingButton> buttons = new ArrayList<>();

    private int x, y;
    private final int w, h;
    private boolean open;
    private int showingModuleCount;

//    private final Color GRADIENT_START = new Color(30, 30, 30, 190);
//    private final Color GRADIENT_END = new Color(25, 25, 25, 190);

    private final Color DARK_GRAY0 = new Color(new java.awt.Color(30, 30, 30, 150).brighter());
    private final Color DARK_GRAY = new Color(30, 30, 30, 150);
    private final Color GRAY = new Color(155, 155, 155, 255);
    private final Color BLACK = new Color(20, 20, 20, 255);
    private final Color WHITE = new Color(255, 255, 255, 255);

    public ModuleButton(Module module, int x, int y, int w, int h) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        int n = 0;

        buttons.add(new BindSetting(module, this.x, this.y + this.h + n, this.w, this.h));
        for (Iterator<SettingGroup> it = module.settings.iterator(); it.hasNext(); ) {
            SettingGroup sg = it.next();

            for (Iterator<Setting<?>> iter = sg.iterator(); iter.hasNext(); ) {
                Setting setting = iter.next();
                SettingButton settingButton = null;
                if (setting instanceof BoolSetting s) {
                    settingButton = new BooleanSetting(module, s, this.x, this.y + this.h + n, this.w, this.h);
                } else if (setting instanceof IntSetting s) {
                    settingButton = new IntegerSetting.Slider(module, s, this.x, this.y + this.h + n, this.w, this.h);
                } else if (setting instanceof dev.lemonclient.settings.DoubleSetting s) {
                    settingButton = new DoubleSetting.Slider(module, s, this.x, this.y + this.h + n, this.w, this.h);
                } else if (setting instanceof EnumSetting s) {
                    settingButton = new ModeSetting(module, s, this.x, this.y + this.h + n, this.w, this.h);
                }

                if (settingButton != null) {
                    buttons.add(settingButton);
                }
                n += this.h;
            }
        }

        //buttons.add(new DrawnSetting(module, this.x, this.y + this.h + n, this.w, this.h));
    }

    @Override
    public void render(int mouseX, int mouseY) {
        MatrixStack stack = vanillaText.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        drawOutline(x, y, w, h);

        boolean hover = isHover(x, y, w, h - 1, mouseX, mouseY);
        if (module.isActive()) {
            drawFlat(x + 2, y + 1, x + w - 2, y + h, hover ? DARK_GRAY0 : Color.WHITE.a(200));
        } else {
            drawFlat(x + 2, y + 1, x + w - 2, y + h, hover ? DARK_GRAY0 : DARK_GRAY);
        }

        vanillaText.drawWShadow(module.title, (float) (x + 5), (float) (y + 4), module.isActive() ? WHITE : GRAY);
        vanillaText.drawWShadow(open ? "-" : "+", (float) (x + 85), (float) (y + 4), module.isActive() ? WHITE : GRAY);
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (isHover(x, y, w, h - 1, mouseX, mouseY)) {
            switch (button) {
                case 0 -> {
                    mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                    module.toggle();
                }
                case 1 -> processRightClick();
            }
        }

        if (open) {
            for (SettingButton settingButton : buttons) {
                settingButton.mouseDown(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseUp(int mouseX, int mouseY) {
        for (SettingButton settingButton : buttons) {
            settingButton.mouseUp(mouseX, mouseY);
        }
    }

    @Override
    public void keyPress(int key) {
        for (SettingButton settingButton : buttons) {
            settingButton.keyPress(key);
        }
    }

    @Override
    public void close() {
        for (SettingButton button : buttons) {
            button.close();
        }
    }

    private boolean isHover(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x * 1.0F && mouseX <= (x + w) * 1.0F && mouseY >= y * 1.0F && mouseY <= (y + h) * 1.0F;
    }

    public void x(int x) {
        this.x = x;
    }
    public void y(int y) {
        this.y = y;
    }
    public boolean open() {
        return this.open;
    }
    public Module module() {
        return module;
    }
    public List<SettingButton> buttons() {
        return this.buttons;
    }
    public int moduleCount() {
        return this.showingModuleCount;
    }

    public void processRightClick() {
        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
        if (!open) {
            showingModuleCount = buttons.size();
            open = true;
        } else {
            showingModuleCount = 0;
            open = false;
        }
    }
}
*/
