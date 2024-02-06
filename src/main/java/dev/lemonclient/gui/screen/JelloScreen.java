package dev.lemonclient.gui.screen;
/*
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.addon.utils.font.CFont;
import dev.lemonclient.addon.utils.font.TTFFontRender;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class JelloScreen extends Screen {
    public static Identifier IDAlt,IDExit,IDLogo,IDMulitPlayer,IDSettings,IDSinglePlayer,IDBg,IDBg1;static {IDAlt = new Identifier("lemon-client", "jello/altmanager.png");IDExit = new Identifier("lemon-client", "jello/exit.png");IDLogo = new Identifier("lemon-client", "jello/jellologo.png");IDMulitPlayer = new Identifier("lemon-client", "jello/multiplayer.png");IDSettings = new Identifier("lemon-client", "jello/settings.png");IDSinglePlayer = new Identifier("lemon-client", "jello/singleplayer.png");IDBg = new Identifier("lemon-client", "jello/bg.png");IDBg1 = new Identifier("lemon-client", "jello/bg1.png");}
    public static float animatedMouseX,animatedMouseY;
    public float zoom1 = 1, zoom2 = 1, zoom3 = 1, zoom4 = 1, zoom5 = 1;
    int i = 1;

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = c.getMatrices();
        // Bg
        c.drawTexture(IDBg, (int) (-1177/2 - 372 - animatedMouseX + this.width), (int) (-34/2 +8 - animatedMouseY/9.5f + this.height/19 - 19), 0, 0, 3840/2, 1180/2, 3840/2, 1180/2);

        //pe.render(new MatrixStack(), animatedMouseX, animatedMouseY, this.width, this.height);

        float offset = (int) (-16 + this.width/2 - 289/2f + 8);
        float height = (int) (this.height/2 + 29/2f - 8 + 1f);

        GL.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Logo
        matrices.push();
        RenderSystem.setShaderTexture(0,IDLogo);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawTexture(IDLogo,c,this.width/2 - 323/4f, this.height/2 - 161/2f + 11 - 32/2f + 1f, 0, 0, 323/2f, 161/2f, 323/2, 161/2);
        matrices.pop();
        // Buttons
        // SinglePlayer
        matrices.push();
        if(isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
            if(zoom1 < 1.2)
                zoom1 += 0.06666666666666666666666666666667;
        }else if (zoom1 > 1) {
            zoom1 -= 0.06666666666666666666666666666667;
        }
        if(zoom1 > 1) {
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1.2, zoom1), (float) Math.min(1.2, zoom1), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.setShaderTexture(0,IDSinglePlayer);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawTexture(IDSinglePlayer,c,offset, height, 0, 0, 64, 64, 64, 64);
        matrices.pop();
        matrices.push();
        if(zoom1 > 1) {
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1, zoom1-.2), (float) Math.min(1, zoom1-.2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            drawString("Singleplayer", (float) (offset + 32 - getStringWidth("Singleplayer")/2 + 1f), height + 140/2 + 1 - 4, new Color(100/255f ,100/255f ,100/255f, Math.max(0, Math.min(1, 1f+(zoom1-1)*2.5f))),Math.min(1.2, zoom1));
        }
        matrices.pop();
        offset += 122/2f;

        // MultiPlayer
        matrices.push();
        if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
            if(zoom2 < 1.2)
                zoom2 += 0.06666666666666666666666666666667;
        }else if (zoom2 > 1) {
            zoom2 -= 0.06666666666666666666666666666667;
        }
        if(zoom2 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1.2, zoom2), (float) Math.min(1.2, zoom2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.setShaderTexture(0,IDMulitPlayer);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawTexture(IDMulitPlayer,c,offset, height, 0, 0, 64, 64, 64, 64);
        matrices.pop();
        matrices.push();
        if(zoom2 > 1) {
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1, zoom2-.2), (float) Math.min(1, zoom2-.2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            drawString("Multiplayer", (float) (offset + 32 - getStringWidth("Multiplayer")/2 + 1f), height + 140/2 + 1 - 4, new Color(100/255f ,100/255f ,100/255f, Math.max(0, Math.min(1, 1f+(zoom2-1)*2.5f))),Math.min(1.2, zoom1));
        }
        matrices.pop();
        offset += 122/2f;

        // Settings
        matrices.push();
        if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
            if(zoom3 < 1.2)
                zoom3 += 0.06666666666666666666666666666667;
        }else if (zoom3 > 1) {
            zoom3 -= 0.06666666666666666666666666666667;
        }
        if(zoom3 > 1) {
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1.2, zoom3), (float) Math.min(1.2, zoom3), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.setShaderTexture(0,IDSettings);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawTexture(IDSettings,c,offset, height, 0, 0, 64, 64, 64, 64);
        matrices.pop();
        matrices.push();
        if(zoom3 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1, zoom3-.2), (float) Math.min(1, zoom3-.2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);

            drawString("Settings", (float) (offset + 32 - getStringWidth("Settings")/2 + 1f), height + 140/2 + 1 - 4, new Color(100/255f ,100/255f ,100/255f, Math.max(0, Math.min(1, 1f+(zoom3-1)*2.5f))),Math.min(1.2, zoom1));
        }
        matrices.pop();
        offset += 122/2f;

        // Alt Manager
        matrices.push();
        if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
            if(zoom4 < 1.2)
                zoom4 += 0.06666666666666666666666666666667;
        }else if (zoom4 > 1) {
            zoom4 -= 0.06666666666666666666666666666667;
        }
        if(zoom4 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1.2, zoom4), (float) Math.min(1.2, zoom4), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.setShaderTexture(0,IDAlt);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        this.drawTexture(IDAlt,c,offset, height, 0, 0, 64, 64, 64, 64);
        matrices.pop();
        matrices.push();
        if(zoom4 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1, zoom4-.2), (float) Math.min(1, zoom4-.2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            drawString("Alt Manager", (float) (offset + 32 - getStringWidth("Alt Manager")/2 + 1f), height + 140/2 + 1 - 4, new Color(100/255f ,100/255f ,100/255f, Math.max(0, Math.min(1, 1f+(zoom4-1)*2.5f))),Math.min(1.2, zoom1));
        }
        matrices.pop();
        offset += 122/2f;

        // Exit
        matrices.push();
        if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
            if(zoom5 < 1.2)
                zoom5 += 0.06666666666666666666666666666667;
        }else if (zoom5 > 1) {
            zoom5 -= 0.06666666666666666666666666666667;
        }

        if(zoom5 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1.2, zoom5), (float) Math.min(1.2, zoom5), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        RenderSystem.setShaderTexture(0,IDExit);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        this.drawTexture(IDExit,c,offset, height, 0, 0, 64, 64, 64, 64);
        matrices.pop();
        matrices.push();
        if(zoom5 > 1){
            matrices.translate(offset + 32, height + 64, 0);
            matrices.scale((float) Math.min(1, zoom5-.2), (float) Math.min(1, zoom5-.2), 1);
            matrices.translate(-(offset + 32), -(height + 64), 0);
            drawString("Exit", (float) (offset + 32 -getStringWidth("Exit")/2 + 1f), height + 140/2 + 1 - 4, new Color(100/255f ,100/255f ,100/255f, Math.max(0, Math.min(1, 1f+(zoom5-1)*2.5f))),Math.min(1.2, zoom1));
        }
        matrices.pop();
        RenderSystem.disableBlend();


        animatedMouseX += ((mouseX-animatedMouseX) / 1.8) + 0.1;
        animatedMouseY += ((mouseY-animatedMouseY) / 1.8) + 0.1;
        super.render(c, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            float offset = -16 + this.width/2 - 289/2f + 8;
            float height = this.height/2 + 29/2f - 8;

            if(this.isMouseHoveringRect1(offset + 4, height + 4, (64-8),  (64-8),  mouseX, mouseY)){
                client.setScreen(new SelectWorldScreen(this));
            }
            offset += 122/2f;
            if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
                Screen screen = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);this.client.setScreen(screen);
            }
            offset += 122/2f;
            if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
            offset += 122/2f;
            if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
                client.setScreen(GuiThemes.get().accountsScreen());
            }
            offset += 122/2f;
            if(this.isMouseHoveringRect1(offset + 4, height + 4, 64-8, 64-8, mouseX, mouseY)){
                client.scheduleStop();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public JelloScreen() {
        super(Text.translatable("narrator.screen.title"));
    }

    public void drawTexture(Identifier t,DrawContext matrices, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        matrices. drawTexture(t,(int) x, (int) y, (int) width, (int) height, u, v, (int) width, (int) height, (int) textureWidth, (int) textureHeight);
    }

    public boolean isMouseHoveringRect1(float x, float y, int width, int height, int mouseX, int mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public boolean isMouseHoveringRect1(float x, float y, int width, int height, double mouseX, double mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public void drawString(String text, float x, float y, Color color,double fontSize) {
        TTFFontRender jelloFont = CFont.jelloLight;
        jelloFont.render(text,x,y,color,fontSize);
    }

    public double getStringWidth(String text) {
        TTFFontRender jelloFont = CFont.jelloLight;
        return jelloFont.getWidth(text,false,0.75);
    }

    public double getStringWidth(String text,boolean shadow) {
        TTFFontRender jelloFont = CFont.jelloLight;
        return jelloFont.getWidth(text,shadow,0.75);
    }

    public double getStringHeight(boolean shadow) {
        TTFFontRender jelloFont = CFont.jelloLight;
        return jelloFont.getHeight(shadow,0.75);
    }
}
*/
