package dev.lemonclient.renderer;

import dev.lemonclient.enums.DrawMode;
import dev.lemonclient.utils.render.color.Color;
import org.joml.Matrix4f;

public class SharedShaderMesh extends Mesh {
    public static SharedShaderMesh BOUND;

    public Shader shader;
    private final Runnable[] uniformsToSet = new Runnable[256];
    private int toSetPosition = 0;

    public SharedShaderMesh(Shader shader, DrawMode drawMode, Attrib... attributes) {
        super(drawMode, attributes);
        this.shader = shader;
    }

    @Override
    protected void beforeRender() {
        if (shader == null) return;
        shader.bind();
        BOUND = this;
    }

    public void setUniforms() {
        for (int i = 0; i < uniformsToSet.length; i++) {
            Runnable task = uniformsToSet[i];
            if (task != null) {
                task.run();
            }
            uniformsToSet[i] = null;
        }
        toSetPosition = 0;
    }

    public void set(String name, boolean v) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, v);
        };
        toSetPosition++;
    }

    public void set(String name, int v) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, v);
        };
        toSetPosition++;
    }

    public void set(String name, double v) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, v);
        };
        toSetPosition++;
    }

    public void set(String name, double v1, double v2) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, v1, v2);
        };
        toSetPosition++;
    }

    public void set(String name, Color color) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, color);
        };
        toSetPosition++;
    }

    public void set(String name, float v1, float v2, float v3, float v4) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, v1, v2, v3, v4);
        };
        toSetPosition++;
    }

    public void set(String name, Matrix4f mat) {
        if (shader == null) return;
        uniformsToSet[toSetPosition] = () -> {
            shader.set(name, mat);
        };
        toSetPosition++;
    }
}
