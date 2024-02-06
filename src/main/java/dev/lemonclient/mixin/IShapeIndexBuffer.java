package dev.lemonclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.ShapeIndexBuffer.class)
public interface IShapeIndexBuffer {
    @Accessor("id")
    int getId();
}
