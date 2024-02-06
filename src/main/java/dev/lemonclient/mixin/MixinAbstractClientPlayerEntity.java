package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.Capes;
import dev.lemonclient.utils.misc.FakeClientPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.lemonclient.LemonClient.mc;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {
    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void onGetCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(Modules.get().get(Capes.class).getCape((AbstractClientPlayerEntity) (Object) this, false));
    }

    @Inject(method = "getElytraTexture", at = @At("HEAD"), cancellable = true)
    private void onGetElytraTexture(CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(Modules.get().get(Capes.class).getCape((AbstractClientPlayerEntity) (Object) this, true));
    }

    @Inject(method = "getPlayerListEntry", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerListEntry(CallbackInfoReturnable<PlayerListEntry> info) {
        if (mc.getNetworkHandler() == null) info.setReturnValue(FakeClientPlayer.getPlayerListEntry());
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> info) {
        if (mc.getNetworkHandler() == null) info.setReturnValue(false);
    }

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true)
    private void onIsCreative(CallbackInfoReturnable<Boolean> info) {
        if (mc.getNetworkHandler() == null) info.setReturnValue(false);
    }
}
