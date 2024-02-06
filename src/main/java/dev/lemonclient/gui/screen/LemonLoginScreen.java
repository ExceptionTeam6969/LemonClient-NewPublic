package dev.lemonclient.gui.screen;
/*
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.client.lemonchat.ChatClient;
import dev.lemonclient.addon.lemonchat.user.UserData;
import dev.lemonclient.utils.lemonchat.GsonUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LemonLoginScreen extends GuiRender {
    private TextFieldWidget usernameWidget;
    private final File data = new File(".lemonchat", "data.json");
    public String status = "";
    private ButtonWidget loginButton;

    public static LemonLoginScreen INSTANCE;

    public LemonLoginScreen() {
        INSTANCE = this;
    }

    @Override
    public void tick() {
        this.usernameWidget.tick();

       // if (ChatClient.INSTANCE.passent) {
          //  client.setScreen(new TitleScreen(true));
        //}
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.loginButton = ButtonWidget.builder(Text.literal("Login"),(b) -> {
            if (usernameWidget.getText().isEmpty()) {
                status = "User name cannot be empty!";
                return;
            }
            ChatClient.INSTANCE.session.login(usernameWidget.getText());
        }).dimensions(width / 2 - 50, height / 2 + 20+20+5,100,20).build();
        this.usernameWidget = new TextFieldWidget(this.textRenderer, width / 2 - 50, height / 2 + 20, 100, 20, Text.literal("Username"));
        this.usernameWidget.setMaxLength(20);
        this.addSelectableChild(usernameWidget);
        this.addDrawableChild(loginButton);

        if (data.exists()) {
            String jsonString = null;
            try {
                jsonString = Files.readString(data.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            GsonBuilder gsonBuilder = GsonUtils.newBuilder();
            Gson gson = gsonBuilder.create();
            UserData userData = gson.fromJson(jsonString, UserData.class);

            if (userData.name.isEmpty()) {
                return;
            }
            usernameWidget.setText(userData.name);
            ChatClient.INSTANCE.session.login(userData.name);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (usernameWidget.getText().isEmpty()) {
                status = "User name cannot be empty!";
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            ChatClient.INSTANCE.session.login(usernameWidget.getText());
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private final Identifier bg = new Identifier("lemon-client", "background.png");

    @Override
    public void draw(DrawContext context, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        context.drawTexture(bg, 0, 0, 0, 0, width, height, width, height);

        this.usernameWidget.render(context, mouseX, mouseY, delta);
        int lw = this.textRenderer.getWidth("Lemon Client");
        int gw = this.textRenderer.getWidth("Login");
        int sw = this.textRenderer.getWidth(status);
        context.drawTextWithShadow(this.textRenderer, "Lemon Client", width / 2 - lw / 2, 100, Color.WHITE.getPacked());
        context.drawTextWithShadow(this.textRenderer, "Login", width / 2 - gw / 2, 100 + this.textRenderer.fontHeight + 1, Color.WHITE.getPacked());

        context.drawTextWithShadow(this.textRenderer, status, width / 2 - sw / 2, usernameWidget.getY() - 20, Color.WHITE.getPacked());
    }

    public static void check() {

    }
}
*/
