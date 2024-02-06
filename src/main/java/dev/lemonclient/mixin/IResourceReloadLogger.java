package dev.lemonclient.mixin;

import net.minecraft.client.resource.ResourceReloadLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourceReloadLogger.class)
public interface IResourceReloadLogger {
    @Accessor("reloadState")
    ResourceReloadLogger.ReloadState getReloadState();
}
