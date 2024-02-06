package dev.lemonclient.bot;

import dev.lemonclient.bot.client.BotClient;
import dev.lemonclient.bot.server.BotServer;

public class BotNet {
    public static BotClient createClient(String ip, int port) {
        return new BotClient(ip, port);
    }

    public static BotServer createServer() {
        return new BotServer();
    }
}
