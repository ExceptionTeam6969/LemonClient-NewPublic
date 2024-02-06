package dev.lemonclient.bot.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class BotClient {
    public final Logger LOGGER = LogManager.getLogger("Bot-Client");
    public String address;
    public int port;

    public boolean running = true;

    public Bootstrap bootstrap;
    public BotSession session;
    private final EventLoopGroup group;
    public static BotClient INSTANCE;

    public BotClient(String address, int port) {
        this.address = address;
        this.port = port;
        INSTANCE = this;

        new Thread(this::update, "BotTicker").start();

        group = new NioEventLoopGroup();
        try {
            Class<? extends Channel> class_;
            if (Epoll.isAvailable()) {
                class_ = EpollSocketChannel.class;
            } else {
                class_ = NioSocketChannel.class;
            }
            this.bootstrap = new Bootstrap().group(group).channel(class_).handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException ignored) {
                    }

                    channel.pipeline()
                        .addLast("encoder", new StringEncoder(StandardCharsets.UTF_8))
                        .addLast("decoder", new StringDecoder(StandardCharsets.UTF_8))
                        .addLast("packet_handler", session = new BotSession(BotClient.this));
                }
            });
        } catch (Exception e) {
            stop();
        }
    }

    public void connect() {
        try {
            bootstrap.connect(address, port);
        } catch (Exception e) {
            stop();
        }
    }

    public void stop() {
        if (session != null) {
            session.disconnect("Disconnect.");
        }
        running = false;
        if (group != null) {
            group.shutdownGracefully();
        }
        bootstrap = null;
        session = null;
        INSTANCE = null;
    }

    private void update() {
        while (running) {
            try {
                if (this.session != null) this.session.tick();

                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
