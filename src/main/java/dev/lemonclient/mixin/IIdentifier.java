package dev.lemonclient.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Identifier.class)
public interface IIdentifier {
    @Mutable
    @Accessor
    void setPath(String path);
}
