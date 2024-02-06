package dev.lemonclient.bot.server;

import dev.lemonclient.lemonchat.utils.ChatFormatting;
import dev.lemonclient.lemonchat.utils.StringUtils;
import dev.lemonclient.utils.Utils;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.lemonclient.LemonClient.mc;

public class BotServer {
    public static BotServer INSTANCE;
    private ServerConnectionListener listener;
    public boolean running;

    public BotServer() {
        INSTANCE = this;
    }

    public void launch(int port) {
        listener = new ServerConnectionListener(port);
        new Thread(listener::launch).start();
        new Thread(this::tick).start();
    }

    public void stop() {
        running = false;
        connections.forEach(SessionConnection::disconnect);
        listener = null;
        INSTANCE = null;
    }

    public final List<SessionConnection> connections = new CopyOnWriteArrayList<>();

    public void channelJoin(SessionConnection connection) {
        info("Client '{}' connected.", connection);
        connections.add(connection);
    }

    public void channelDisconnect(SessionConnection connection) {
        info("Client '{}' disconnect.", connection.address);
        this.connections.remove(connection);
    }

    public void info(String msg, Object... args) {
        msg = StringUtils.getReplaced(msg, args);
        if (Utils.canUpdate()) {
            msg = "{}[{}SBot{}Net{}] {}" + msg;
            msg = StringUtils.getReplaced(msg, ChatFormatting.GRAY, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.GRAY, ChatFormatting.RESET);
            mc.inGameHud.getChatHud().addMessage(Text.literal(msg));
        }
    }

    public void tick() {
        while (running) {
            try {
                connections.forEach(SessionConnection::tick);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
