package dev.lemonclient.bot.server;

import dev.lemonclient.bot.ActionRegistry;
import dev.lemonclient.bot.BotAction;
import dev.lemonclient.bot.actions.DisconnectAction;
import dev.lemonclient.lemonchat.utils.GsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;

import java.net.SocketAddress;
import java.util.Objects;

public class SessionConnection extends SimpleChannelInboundHandler<String> {
    public SocketAddress address;
    public Channel channel;
    public boolean disconnectionHandled;

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        this.address = channel.remoteAddress();
        this.channel.config().setAutoRead(true);

        BotServer.INSTANCE.channelJoin(this);
    }

    public void channelInactive(ChannelHandlerContext p_129527_) {
        this.disconnect("Disconnect.");
    }

    public void disconnect() {
        this.disconnect("Disconnect.");
    }

    public void disconnect(String reason, Object... args) {
        for (Object arg : args) {
            reason = reason.replaceFirst("\\{}", arg.toString());
        }
        if (this.channel.isOpen()) {
            this.send(new DisconnectAction(reason));
            BotServer.INSTANCE.channelDisconnect(this);

            this.channel.close().awaitUninterruptibly();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        if (!channel.isOpen()) return;
        BotAction action = GsonUtils.jsonToBean(json, BotAction.class);
        if (action != null) {
            String name = action.name;
            Class<? extends BotAction> klass = ActionRegistry.get(name);
            if (klass != null) {
                BotAction instance = klass.getConstructor(BotAction.class).newInstance(action);
                instance.init();
                instance.execute();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.channel.isOpen()) {
            if (cause instanceof TimeoutException) {
                this.disconnect("Timed out.");
            }
        }
    }

    public void tick() {
        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }

        if (this.channel != null) {
            this.channel.flush();
        }
    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            this.disconnectionHandled = true;
            BotServer.INSTANCE.channelDisconnect(this);
        }
    }

    public void send(BotAction action) {
        if (action != null) {
            action.isClient = false;
            this.channel.writeAndFlush(GsonUtils.beanToJson(action));
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionConnection that = (SessionConnection) o;

        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }
}
