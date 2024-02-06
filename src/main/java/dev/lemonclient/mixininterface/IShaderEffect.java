package dev.lemonclient.mixininterface;

import net.minecraft.client.gl.Framebuffer;

public interface IShaderEffect {
    void lemonclient$addFakeTargetHook(String name, Framebuffer buffer);
}
