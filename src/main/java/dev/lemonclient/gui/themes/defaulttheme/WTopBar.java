package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.tabs.Tab;
import dev.lemonclient.gui.tabs.TabScreen;
import dev.lemonclient.gui.tabs.Tabs;
import dev.lemonclient.gui.widgets.pressable.WPressable;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;

import static dev.lemonclient.LemonClient.mc;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public class WTopBar extends dev.lemonclient.gui.widgets.WTopBar implements LCGuiWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }

    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            add(new WTopBarButton(tab));
        }
    }

    protected int getState(WTopBarButton btn) {
        int a = 0;
        if (btn.equals(cells.get(0).widget()))
            a |= 1;
        if (btn.equals(cells.get(cells.size() - 1).widget()))
            a |= 2;
        return a;
    }

    protected class WTopBarButton extends WPressable {
        private final Tab tab;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme.textWidth(tab.name) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            Screen screen = mc.currentScreen;

            if (!(screen instanceof TabScreen) || ((TabScreen) screen).tab != tab) {
                double mouseX = mc.mouse.getX();
                double mouseY = mc.mouse.getY();

                tab.openScreen(theme);
                glfwSetCursorPos(mc.getWindow().getHandle(), mouseX, mouseY);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            Color color = getButtonColor(pressed || (mc.currentScreen instanceof TabScreen && ((TabScreen) mc.currentScreen).tab == tab), mouseOver);

            //renderer.quad(x, y, width, height, color);
            switch (getState(this)) {
                case 1 -> renderer.quadRoundedSide(this, color, ((LCGuiTheme) theme).roundAmount(), false);
                case 2 -> renderer.quadRoundedSide(this, color, ((LCGuiTheme) theme).roundAmount(), true);
                case 3 -> renderer.quadRounded(this, color, ((LCGuiTheme) theme).roundAmount());
                default -> renderer.quad(this, color);
            }
            renderer.text(tab.name, x + pad, y + pad, getNameColor(), false);
        }
    }
}
