package dev.lemonclient.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface IBufferBuilder {
    @Accessor("elementOffset")
    int getElementOffset();

    @Accessor("elementOffset")
    void setElementOffset(int elementOffset);

    @Accessor("format")
    VertexFormat getVertexFormat();
}
