package dev.lemonclient.renderer;

import dev.lemonclient.enums.DrawMode;

public class ShaderMesh extends Mesh {
    private final Shader shader;

    public ShaderMesh(Shader shader, DrawMode drawMode, Attrib... attributes) {
        super(drawMode, attributes);

        this.shader = shader;
    }

    @Override
    protected void beforeRender() {
        shader.bind();
    }
}
