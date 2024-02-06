package dev.lemonclient.mixin;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(PlayerSkinProvider.class)
public interface IPlayerSkinProvider {
    @Accessor("skinCacheDir")
    File getSkinCache();
}
