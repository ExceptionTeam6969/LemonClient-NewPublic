package dev.lemonclient.gui.screen.harvest;
/*
import dev.lemonclient.addon.gui.screen.harvest.button.ModuleButton;
import dev.lemonclient.addon.gui.screen.harvest.button.SettingButton;
import dev.lemonclient.systems.modules.Category;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;

public class Window extends Component {
    private final ArrayList<ModuleButton> buttons = new ArrayList<>();
    private final Category category;
    private final ArrayList<ModuleButton> buttonsBeforeClosing = new ArrayList<>();
    private int x;
    private int y;
    private final int w;
    private final int h;
    private int dragX;
    private int dragY;
    private boolean open = true;
    private boolean dragging;
    private int totalHeightForBars;
    private int showingButtonCount;

    private final int BLACK = new Color(20, 20, 20, 255).getPacked();

    public Window(Category category, int x, int y, int w, int h) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        int yOffset = this.y + this.h;
        for (Module module : Modules.get().getGroup(category)) {
            ModuleButton button = new ModuleButton(module, this.x, yOffset, this.w, this.h);
            buttons.add(button);
            yOffset += this.h;
            totalHeightForBars++;
        }
        showingButtonCount = buttons.size();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        if (dragging) {
            x = dragX + mouseX;
            y = dragY + mouseY;
        }

        drawEar(true, x - 8, y - 8);
        drawEar(false, x + w - 12, y - 8);

        drawFlat(x, y, x + w, y + h, new Color(255,255,255,200));

        MatrixStack stack = renderer.getMatrixStack();
        stack.scale(1.0F, 1.0F, 1.0F);

        vanillaText.drawWShadow(category.name, x + 4, y + 4, -1);
        String moduleCount = "[" + Modules.get().getGroup(category).size() + "]";
        vanillaText.drawWShadow(moduleCount, x + 93 - vanillaText.width(moduleCount), y + 4, new Color(-1));

        if (open) {
            renderer.drawRect(x, y + h, w, h + 1, BLACK);

            int modY = y + h + 1;
            totalHeightForBars = 0;

            int moduleRenderCount = 0;
            for (ModuleButton moduleButton : buttons) {
                moduleButton.renderer = renderer;
                moduleButton.vanillaText = vanillaText;
                moduleButton.context = context;
                moduleButton.clientText = clientText;

                moduleRenderCount++;

                if (moduleRenderCount < showingButtonCount + 1) {
                    moduleButton.x(x);
                    moduleButton.y(modY);

                    moduleButton.render(mouseX, mouseY);

                    modY += h;
                    totalHeightForBars++;

                    if (moduleButton.open()) {

                        int settingRenderCount = 0;
                        for (SettingButton settingButton : moduleButton.buttons()) {
                            settingButton.renderer = renderer;
                            settingButton.vanillaText = vanillaText;
                            settingButton.context = context;
                            settingButton.clientText = clientText;
                            settingRenderCount++;

                            if (settingRenderCount < moduleButton.moduleCount() + 1) {
                                settingButton.x(x);
                                settingButton.y(modY);

                                settingButton.render(mouseX, mouseY);

                                modY += h;
                                totalHeightForBars++;
                            }
                        }
                    }
                }
            }


            renderer.drawRect(x, y + h + ((totalHeightForBars) * h) + 2, w, h + ((totalHeightForBars) * h) + 3, BLACK);
        }
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (hover(x, y, w, h, mouseX, mouseY)) {
            switch (button) {
                case 0 -> {
                    dragging = true;
                    dragX = x - mouseX;
                    dragY = y - mouseY;
                }
                case 1 -> {
                    if (open) {
                        showingButtonCount = buttons.size();
                        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                        open = false;
                        for (ModuleButton b : buttons) {
                            if (b.open()) {
                                b.processRightClick();
                                buttonsBeforeClosing.add(b);
                            }
                        }
                    } else {
                        showingButtonCount = buttons.size();
                        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                        open = true;
                        buttonsBeforeClosing.clear();
                    }
                }
            }
        }

        if (open) {
            for (ModuleButton b : buttons) {
                b.mouseDown(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseUp(int mouseX, int mouseY) {
        dragging = false;

        if (open) {
            for (ModuleButton button : buttons) {
                button.mouseUp(mouseX, mouseY);
            }
        }
    }

    @Override
    public void keyPress(int key) {
        if (open) {
            for (ModuleButton button : buttons) {
                button.keyPress(key);
            }
        }
    }

    @Override
    public void close() {
        for (ModuleButton button : buttons) {
            button.close();
        }
    }

    private boolean hover(int X, int Y, int W, int H, int mouseX, int mouseY) {
        return mouseX >= X * 1.0F && mouseX <= (X + W) * 1.0F && mouseY >= Y * 1.0F && mouseY <= (Y + H) * 1.0F;
    }

    public Category category() {
        return category;
    }
    public int x() {
        return x;
    }
    public int y() {
        return y;
    }
    public void y(int y) {
        this.y = y;
    }
    public int h() {
        return h;
    }
}
*/
