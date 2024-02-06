package dev.lemonclient.gui.screen;
/*
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.addon.LemonClient;
import dev.lemonclient.utils.render.utils.BezierCurve;
import dev.lemonclient.utils.render.utils.MSAAFramebuffer;
import dev.lemonclient.utils.render.utils.Render2DUtils;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.gui.tabs.Tabs;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import static dev.lemonclient.LemonClient.mc;

public class LCScreen extends GuiRender {
    private final BezierCurve aniCur = new BezierCurve(.35, .1, .25, 1);
    private boolean loaded;

    private final double buttonHeight = (double) 30 / 2;
    private final double buttonWidth = (double) 185 / 2;
    private final double buttonOffset = 6.5 / 2;
    private final Color[] colors = new Color[]{
        new Color(0, 0, 0, 255), // Rect Color
        new Color(27, 52, 53, 170), // Outline Color
        new Color(255, 255, 240, 170) // Outline Hover Color
    };

    private final String[] buttons = new String[]{
        "Singleplayer",
        "Multiplayer",
        "Alt Manager",
        "Options",
        "Languages",
        "Click GUI",
        "Quit Game"
    };

    private final Identifier logo = new Identifier("lemon-client", "icons/icon_100x100.png");
    private final Identifier bg = new Identifier("lemon-client", "background.png");

    private int ticks = 0;

    private double percent = 0, lastPercent = percent;

    public LCScreen() {}

    @Override
    protected void init() {
        ticks = 0;
        super.init();
    }

    @Override
    public void tick() {
        lastPercent = percent;

        if (hov) {
            ticks = 0;
        }

        if (ticks >= 5000) {
            if (loaded) {
                loaded = false;
            }
        } else {
            ticks++;
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            loaded = !loaded;
        }

        double centerA = (this.width / 2D);
        double centerB = (this.height / 2D);
        double centerX = centerA - (buttonWidth / 2);
        double centerY = centerB - (buttonHeight / 2);

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            double y = (-((buttonHeight * 2) + (buttonOffset * 2)) + centerY) * percent;
            for (String name : buttons) {
                boolean hovered = isMouseHoveringRect(centerX, y, buttonWidth, buttonHeight, mouseX, mouseY);
                if (hovered) {
                    switch (name) {
                        case "Singleplayer" -> mc.setScreen(new SelectWorldScreen(this));
                        case "Multiplayer" -> {
                            if (!mc.options.skipMultiplayerWarning) {
                                mc.options.skipMultiplayerWarning = true;
                                mc.options.write();
                            }
                            Screen screen = new MultiplayerScreen(this);
                            mc.setScreen(screen);
                        }
                        case "Alt Manager" -> mc.setScreen(GuiThemes.get().accountsScreen());
                        case "Options" -> mc.setScreen(new OptionsScreen(this, mc.options));
                        case "Languages" -> mc.setScreen(new LanguageOptionsScreen(this, mc.options, mc.getLanguageManager()));
                        case "Click GUI" -> Tabs.get().get(0).openScreen(GuiThemes.get());
                        case "Quit Game" -> mc.stop();
                        default -> System.out.println(name + " Button Clicked");
                    }
                }
                y += (buttonHeight + buttonOffset);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hov;

    public double textWidth(String s) {
        TextRenderer text = TextRenderer.get();

        text.begin(fontScale);
        double width = text.getWidth(s);
        text.end();
        return width;
    }

    public double textHeight() {
        TextRenderer text = TextRenderer.get();

        text.begin(fontScale);
        double height = text.getHeight(false);
        text.end();
        return height;
    }

    public void text(String s, double x, double y, Color color) {
        TextRenderer text = TextRenderer.get();

        text.begin(fontScale);
        text.render(s,x,y,color);
        text.end();
    }

    public void centerText(String s, double x, double y, Color color) {
        TextRenderer text = TextRenderer.get();

        text.begin(fontScale);
        double fX = text.getWidth(s);
        double sX = x - (fX / 2);

        text.render(s,sX,y,color);
        text.end();
    }

    double fontScale = 0.6;

    @Override
    public void draw(DrawContext drawContext, MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (Utils.firstTimeTitleScreen) {
            Utils.firstTimeTitleScreen = false;
        }

        loaded = true;
        double centerA = (this.width / 2D);
        double centerB = (this.height / 2D);
        double centerX = centerA - (buttonWidth / 2);
        double centerY = centerB - (buttonHeight / 2);

        double Y = (-((buttonHeight * 2) + (buttonOffset * 2)) + centerY);
        for (String name : buttons) {
            boolean hovered = isMouseHoveringRect(centerX, Y, buttonWidth, buttonHeight + buttonOffset, mouseX, mouseY);
            this.hov = hovered;
            if (hovered) {
                if (!loaded) {
                    loaded = true;
                }
            }
            Y += (buttonHeight + buttonOffset);
        }

        // bg render
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawContext.drawTexture(bg, 0, 0, 0, 0, width, height, width, height);

        if (this.loaded) percent = aniCur.get(false, 12);
        else percent = aniCur.get(true, 12);

        if (percent != 0) {
            // Logo Render
            // calc pos
            double logoScale = 40;
            double logoX = centerA - (logoScale / 2);
            double logoY;

            logoY = ((-((buttonHeight * 2) + (buttonOffset * 2)) + centerY) - 80) * smoothTrans(lastPercent, percent);

            // Draw using mesh
            HudRenderer.INSTANCE.texture(logo, logoX, logoY + 30, logoScale, logoScale, Color.WHITE);

            // MC RenderSystem
            boolean meshRender = true;
            if (!meshRender) {
                RenderSystem.setShaderTexture(0, logo);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                drawContext.drawTexture(logo, (int) logoX, (int) logoY, 0, 0, (int) logoScale, (int) logoScale, (int) logoScale, (int) logoScale);
            }

            MSAAFramebuffer.use(() -> {
                // Render Buttons
                double y = (-((buttonHeight * 2) + (buttonOffset * 2)) + centerY) * smoothTrans(lastPercent, percent);
                for (String name : buttons) {
                    // render quad box
                    double buttonRoundRadius = 3;
                    Renderer2D.COLOR.begin();
                    boolean hovered = isMouseHoveringRect(centerX, y, buttonWidth, buttonHeight, mouseX, mouseY);
                    Render2DUtils.quadRoundedOutline(centerX, y, buttonWidth, buttonHeight, hovered ? colors[2] : colors[1], buttonRoundRadius - 0.1, 0.5);
                    Render2DUtils.quadRounded(centerX + 0.5, y + 0.5, buttonWidth - 0.5 * 2, buttonHeight - 0.5 * 2, buttonRoundRadius, colors[0]);
                    Renderer2D.COLOR.render(null);

                    Color fontColor = Color.WHITE;
                    double fY = y + (buttonHeight / 2) - (textHeight() / 2);
                    centerText(name, centerA, fY, fontColor);
                    y += (buttonHeight + buttonOffset);
                }
            });
        }

        // render info text
        String copyright = "Copyright (c) LemonClient Development. Do not distribute!";
        String versionInfo = "Minecraft " + SharedConstants.getGameVersion().getName();
        if (mc.isDemo()) {
            versionInfo = versionInfo + " Demo";
        } else {
            versionInfo = versionInfo + ("release".equalsIgnoreCase(mc.getVersionType()) ? "" : "/" + mc.getVersionType());
        }
        if (MinecraftClient.getModStatus().isModded()) {
            versionInfo = versionInfo + I18n.translate("menu.modded");
        }

        //String updateInfo = "(Latest)";
        String clientInfo = LemonClient.ADDON + " " + LemonClient.VERSION;
        double fontHeight = textHeight();
        // render copyright --- left
        double textX = 0.2;
        double textOffset = (fontHeight + 0.5);
        double textY = this.height - textOffset + 1;
        text(copyright, (float) textX, (float) textY, Color.WHITE);
        textY = textY - textOffset;
        text(versionInfo, (float) textX, (float) textY, Color.WHITE);
        textY = textY - textOffset;
        text(clientInfo, (float) textX, (float) textY, Color.WHITE);
        //double tempWidth = textWidth(clientInfo) + 0.5;
        //text(updateInfo, tempWidth, textY, Color.GREEN);

        // render ciu -- right
        String clientDevInfo = LemonClient.ADDON + " is developed by Fin & WuMie";
        String userInfo = "Welcome, " + mc.getSession().getUsername();

        textX = this.width - textWidth(clientDevInfo);
        textY = this.height - textOffset + 1;
        text(clientDevInfo, (float) textX, (float) textY, Color.WHITE);
        textX = this.width - textWidth(userInfo);
        textY = textY - textOffset;
        text(userInfo, (float) textX, (float) textY, Color.WHITE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private float smoothTrans(double current, double last){
        return (float) (current * mc.getTickDelta() + (last * (1.0f - mc.getTickDelta())));
    }

    private boolean isMouseHoveringRect(double x, double y, double w, double h, double mouseX, double mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x+w && mouseY <= y+h;
    }
}
*/
