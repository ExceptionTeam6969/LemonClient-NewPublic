package dev.lemonclient.mixin;

import net.minecraft.client.font.TextHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextHandler.class)
public interface ITextHandler {
    @Accessor("widthRetriever")
    TextHandler.WidthRetriever getWidthRetriever();
}
