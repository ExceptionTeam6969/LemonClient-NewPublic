package dev.lemonclient.renderer.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.mixin.IPostEffectProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class PostShader {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected PostEffectProcessor shader;
    public Consumer<PostShader> initCallback;
    private final Identifier location;

    private final Map<String, Framebuffer> frameMap = new HashMap<>(256);

    public PostShader(Identifier id, Consumer<PostShader> initCallback) {
        this.initCallback = initCallback;
        location = id;
        initShader();
    }

    public ShaderUniform set(String name) {
        return findUniform(name);
    }

    public void render(float tickDelta) {
        PostEffectProcessor sg = this.getShader();
        if (sg != null) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            sg.render(tickDelta);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableBlend();
            RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
            RenderSystem.enableDepthTest();
        }
    }

    protected void setup() {
        if (initCallback != null)
            initCallback.accept(this);

        shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
    }

    protected ShaderUniform findUniform(String name) {
        if (shader == null) {
            initShader();
        }
        final List<Uniform> uniforms = new ArrayList<>();
        for (PostEffectPass pass : ((IPostEffectProcessor) shader).getPasses()) {
            JsonEffectShaderProgram program = pass.getProgram();
            uniforms.add(program.getUniformByNameOrDummy(name));
        }
        return new ShaderUniform(uniforms);
    }

    public PostEffectProcessor getShader() {
        if (shader == null) {
            initShader();
        }

        return shader;
    }

    protected PostEffectProcessor parseShader(MinecraftClient mc, Identifier location) throws IOException {
        return new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
    }

    private void initShader() {
        try {
            this.shader = parseShader(mc, location);
            shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());

            if (initCallback != null)
                initCallback.accept(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialized post shader program", e);
        }
    }

    public void set(String name, int value) {
        this.set(name).set(value);
    }

    public void set(String name, float value) {
        this.set(name).set(value);
    }

    public void set(String name, float v0, float v1) {
        this.set(name).set(v0, v1);
    }

    public void set(String name, float v0, float v1, float v2, float v3) {
        this.set(name).set(v0, v1, v2, v3);
    }

    public void set(String name, float v0, float v1, float v2) {
        this.set(name).set(v0, v1, v2);
    }
}
