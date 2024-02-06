package dev.lemonclient.utils.render.shaders;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.game.WindowResizedEvent;
import dev.lemonclient.utils.render.color.Color;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL30;

public class RectProgram extends GlProgram {
    private GlUniform color;
    private Framebuffer input;

    public RectProgram() {
        super("rect", VertexFormats.POSITION);
        LemonClient.EVENT_BUS.subscribe(new ConsumerListener(WindowResizedEvent.class, (event) -> {
            if (this.input == null) return;
            this.input.resize(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }));
    }

    public void setParameters(Color color) {
        this.color.set(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f);
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
        this.color = this.findUniform("color");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
