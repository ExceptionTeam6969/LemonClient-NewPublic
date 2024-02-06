package dev.lemonclient.utils.render.shaders;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.game.WindowResizedEvent;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL30;

public class MainMenuProgram extends GlProgram {
    private GlUniform Time;
    private GlUniform uSize;
    private GlUniform Type;
    private Framebuffer input;
    private final long startMS;

    public MainMenuProgram() {
        super("mainmenu", VertexFormats.POSITION);
        startMS = System.currentTimeMillis();
        LemonClient.EVENT_BUS.subscribe(new ConsumerListener<>(WindowResizedEvent.class, event -> {
            if (this.input == null) return;
            this.input.resize(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }));
    }

    public void setParameters(float x, float y, float width, float height, int type) {
        this.uSize.set(width * 2, height * 2);
        this.Time.set((System.currentTimeMillis() - startMS) / 1000F);
        this.Type.set(type);
    }

    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);
        super.use();
    }

    @Override
    protected void setup() {
        this.uSize = this.findUniform("uSize");
        this.Time = this.findUniform("Time");
        this.Type = this.findUniform("Type");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
