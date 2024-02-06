package dev.lemonclient.bot.client;

import dev.lemonclient.bot.ActionRegistry;
import dev.lemonclient.bot.BotAction;
import dev.lemonclient.bot.actions.DisconnectAction;
import dev.lemonclient.lemonchat.utils.ChatFormatting;
import dev.lemonclient.lemonchat.utils.GsonUtils;
import dev.lemonclient.lemonchat.utils.StringUtils;
import dev.lemonclient.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.text.Text;

import static dev.lemonclient.LemonClient.mc;

public class BotSession extends SimpleChannelInboundHandler<String> {
    public final BotClient client;
    public Channel channel;
    private boolean disconnectionHandled;

    public BotSession(BotClient client) {
        this.client = client;
    }

    public void send(BotAction action) {
        if (action != null) {
            action.isClient = true;
            this.channel.writeAndFlush(GsonUtils.beanToJson(action));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        info("Successes connect to '{}'", channel.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String json) throws Exception {
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

    public void tick() {
        if (isConnected() && !disconnectionHandled) {
            handleDisconnection();
        }

        if (channel != null) {
            channel.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (this.channel.isOpen()) {
            if (cause instanceof TimeoutException) {
                client.LOGGER.debug("Timeout", cause);
                this.disconnect("Timed out.");
            }
        }
    }

    public void disconnect(String reason, Object... args) {
        reason = StringUtils.getReplaced(reason, args);
        if (this.channel.isOpen()) {
            this.send(new DisconnectAction(reason));
            info("Disconnect: " + reason);

            this.channel.close().awaitUninterruptibly();
        }
    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            info("Disconnect.");
            disconnectionHandled = true;
            client.stop();
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public void info(String msg, Object... args) {
        msg = StringUtils.getReplaced(msg, args);
        if (Utils.canUpdate()) {
            msg = "{}[{}CBot{}Net{}] {}" + msg;
            msg = StringUtils.getReplaced(msg, ChatFormatting.GRAY, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.GRAY, ChatFormatting.RESET);
            mc.inGameHud.getChatHud().addMessage(Text.literal(msg));
        }
    }
}
