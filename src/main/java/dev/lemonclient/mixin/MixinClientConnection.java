package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.ServerConnectEndEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.AntiPacketKick;
import dev.lemonclient.systems.proxies.Proxies;
import dev.lemonclient.systems.proxies.Proxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;
import java.util.Iterator;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if (packet instanceof BundleS2CPacket bundle) {
            for (Iterator<Packet<ClientPlayPacketListener>> it = bundle.getPackets().iterator(); it.hasNext(); ) {
                if (LemonClient.EVENT_BUS.post(PacketEvent.Receive.get(it.next())).isCancelled()) it.remove();
            }
        } else if (LemonClient.EVENT_BUS.post(PacketEvent.Receive.get(packet)).isCancelled()) info.cancel();
    }

    @Inject(method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"))
    private static void onConnect(InetSocketAddress address, boolean useEpoll, ClientConnection connection, CallbackInfoReturnable<?> cir) {
        LemonClient.EVENT_BUS.post(ServerConnectEndEvent.get(address));
    }

    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
    private void onSendPacketHead(Packet<?> packet, CallbackInfo info) {
        if (LemonClient.EVENT_BUS.post(PacketEvent.Send.get(packet)).isCancelled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("TAIL"))
    private void onSendPacketTail(Packet<?> packet, CallbackInfo info) {
        LemonClient.EVENT_BUS.post(PacketEvent.Sent.get(packet));
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        AntiPacketKick apk = Modules.get().get(AntiPacketKick.class);
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && apk.catchExceptions()) {
            if (apk.logExceptions.get()) apk.warning("Caught exception: %s", throwable);
            ci.cancel();
        }
    }

    @Inject(method = "addHandlers", at = @At("RETURN"))
    private static void onAddHandlers(ChannelPipeline pipeline, NetworkSide side, CallbackInfo ci) {
        if (side != NetworkSide.CLIENTBOUND) return;

        Proxy proxy = Proxies.get().getEnabled();
        if (proxy == null) return;

        switch (proxy.type.get()) {
            case Socks4 ->
                pipeline.addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.address.get(), proxy.port.get()), proxy.username.get()));
            case Socks5 ->
                pipeline.addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.address.get(), proxy.port.get()), proxy.username.get(), proxy.password.get()));
        }
    }
}
