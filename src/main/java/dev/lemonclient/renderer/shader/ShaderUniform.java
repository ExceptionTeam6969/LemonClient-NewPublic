package dev.lemonclient.renderer.shader;

import net.minecraft.client.gl.Uniform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class ShaderUniform {
    private final List<Uniform> uniforms;

    public ShaderUniform(List<Uniform> uniforms) {
        this.uniforms = uniforms;
    }

    public void set(float value1) {
        uniforms.forEach((u) -> u.set(value1));
    }

    public void set(float value1, float value2) {
        uniforms.forEach((u) -> u.set(value1, value2));
    }

    public void set(float value1, float value2, float value3) {
        uniforms.forEach((u) -> u.set(value1, value2, value3));
    }

    public void setAndFlip(float value1, float value2, float value3, float value4) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4));
    }

    public void setForDataType(float value1, float value2, float value3, float value4) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4));
    }

    public void setForDataType(int value1, int value2, int value3, int value4) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4));
    }

    public void set(int value) {
        uniforms.forEach((u) -> u.set(value));
    }

    public void set(int value1, int value2) {
        uniforms.forEach((u) -> u.set(value1, value2));
    }

    public void set(int value1, int value2, int value3) {
        uniforms.forEach((u) -> u.set(value1, value2, value3));
    }

    public void set(int value1, int value2, int value3, int value4) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4));
    }

    public void set(float[] values) {
        uniforms.forEach((u) -> u.set(values));
    }

    public void set(Vector3f vector) {
        uniforms.forEach((u) -> u.set(vector));
    }

    public void set(Vector4f vec) {
        uniforms.forEach((u) -> u.set(vec));
    }

    public void set(float value1, float value2, float value3, float value4) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4));
    }

    public void set(float value1, float value2, float value3, float value4, float value5, float value6) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4, value5, value6));
    }

    public void set(float value1, float value2, float value3, float value4, float value5, float value6, float value7, float value8) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4, value5, value6, value7, value8));
    }


    public void set(float value1, float value2, float value3, float value4, float value5, float value6, float value7, float value8, float value9) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4, value5, value6, value7, value8, value9));
    }

    public void set(float value1, float value2, float value3, float value4, float value5, float value6, float value7, float value8, float value9, float value10, float value11, float value12) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12));
    }

    public void set(float value1, float value2, float value3, float value4, float value5, float value6, float value7, float value8, float value9, float value10, float value11, float value12, float value13, float value14, float value15, float value16) {
        uniforms.forEach((u) -> u.set(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12, value13, value14, value15, value16));
    }

    public void set(Matrix4f matrix4f) {
        uniforms.forEach((u) -> u.set(matrix4f));
    }

    public void set(Matrix3f matrix3f) {
        uniforms.forEach((u) -> u.set(matrix3f));
    }
}
