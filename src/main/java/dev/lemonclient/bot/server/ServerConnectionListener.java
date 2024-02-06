package dev.lemonclient.bot.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConnectionListener {
    protected final Logger LOGGER = LogManager.getLogger("BotServer");

    public final int port;
    public Channel channel;

    public ServerConnectionListener(int port) {
        this.port = port;
    }

    public void launch() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        Class<? extends ServerChannel> class_;
        if (Epoll.isAvailable()) {
            class_ = EpollServerSocketChannel.class;
        } else {
            class_ = NioServerSocketChannel.class;
        }
        try {
            this.channel = new ServerBootstrap().group(boss, worker).channel(class_)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        channel.pipeline()
                            .addLast(new StringEncoder())
                            .addLast(new StringDecoder())
                            .addLast(new SessionConnection());
                    }
                }).bind(port).sync().channel();

            this.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
