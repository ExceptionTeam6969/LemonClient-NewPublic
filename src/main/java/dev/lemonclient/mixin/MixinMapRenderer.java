package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.utils.misc.EmptyIterator;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.MapTexture.class)
public class MixinMapRenderer {
    @ModifyExpressionValue(method = "draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;getIcons()Ljava/lang/Iterable;"))
    private Iterable<MapIcon> getIconsProxy(Iterable<MapIcon> original) {
        return (Modules.get().get(NoRender.class).noMapMarkers()) ? EmptyIterator::new : original;
    }
}
