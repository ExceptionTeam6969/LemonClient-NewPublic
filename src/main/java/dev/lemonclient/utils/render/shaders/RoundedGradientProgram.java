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

public class RoundedGradientProgram extends GlProgram {

    private GlUniform uSize;
    private GlUniform uLocation;
    private GlUniform size;
    private GlUniform color1;
    private GlUniform color2;
    private GlUniform color3;
    private GlUniform color4;

    private Framebuffer input;

    public RoundedGradientProgram() {
        super("gradientround", VertexFormats.POSITION);
        LemonClient.EVENT_BUS.subscribe(new ConsumerListener(WindowResizedEvent.class, (event) -> {
            if (this.input == null) return;
            this.input.resize(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }));
    }

    public void setParameters(float x, float y, float width, float height, float radius, Color color1, Color color2, Color color3, Color color4) {
        this.size.set(radius * 2);
        this.uSize.set(width * 2, height * 2);
        this.uLocation.set(x * 2, -y * 2 + mc.getWindow().getScaledHeight() * 2 - height * 2);
        this.color1.set(color1.r / 255f, color1.g / 255f, color1.b / 255f, color1.a / 255f);
        this.color2.set(color2.r / 255f, color2.g / 255f, color2.b / 255f, color2.a / 255f);
        this.color3.set(color3.r / 255f, color3.g / 255f, color3.b / 255f, color3.a / 255f);
        this.color4.set(color4.r / 255f, color4.g / 255f, color4.b / 255f, color4.a / 255f);

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
        this.uLocation = this.findUniform("uLocation");
        this.size = this.findUniform("Size");
        this.color1 = this.findUniform("color1");
        this.color2 = this.findUniform("color2");
        this.color3 = this.findUniform("color3");
        this.color4 = this.findUniform("color4");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
