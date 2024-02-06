package dev.lemonclient.mixin;

import dev.lemonclient.managers.Managers;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.PingSpoof;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientCommonNetworkHandler {
    @Inject(method = "onKeepAlive", at = @At("HEAD"), cancellable = true)
    private void keepAlive(KeepAliveS2CPacket packet, CallbackInfo ci) {
        if (!Modules.get().isActive(PingSpoof.class) || !Modules.get().get(PingSpoof.class).keepAlive.get()) return;

        ci.cancel();
        Managers.PING_SPOOF.addKeepAlive(packet.getId());
    }

    @Inject(method = "onPing", at = @At("HEAD"), cancellable = true)
    private void pong(PlayPingS2CPacket packet, CallbackInfo ci) {
        if (!Modules.get().isActive(PingSpoof.class) || !Modules.get().get(PingSpoof.class).pong.get()) return;

        ci.cancel();
        Managers.PING_SPOOF.addPong(packet.getParameter());
    }
}
