package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.events.game.SendMessageEvent;
import dev.lemonclient.lemonchat.client.ChatClient;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.lemonclient.LemonClient.mc;

public class IRCCommand extends Command {
    private final MessageHandler handler = new MessageHandler();

    public IRCCommand() {
        super("irc", "Lemon chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(literal("bind").executes((context -> {
                LemonClient.EVENT_BUS.subscribe(handler);
                return SINGLE_SUCCESS;
            }))
        );

        literalArgumentBuilder.then(literal("unbind").executes((context -> {
                LemonClient.EVENT_BUS.unsubscribe(handler);
                return SINGLE_SUCCESS;
            }))
        );

        literalArgumentBuilder.then(literal("disconnect").executes((context -> {
                LemonClient.EVENT_BUS.unsubscribe(handler);
                if (ChatClient.get().session != null)
                    ChatClient.get().session.disconnect("Disconnect.");
                return SINGLE_SUCCESS;
            }))
        );

        literalArgumentBuilder.then(literal("chat").then(argument("message", StringArgumentType.string()).executes((context -> {
                ChatClient.get().chat(StringArgumentType.getString(context, "message"));
                return SINGLE_SUCCESS;
            })))
        );

        literalArgumentBuilder.then(literal("login").executes((context -> {
                if (Utils.canUpdate()) {
                    if (ChatClient.get().session != null) {
                        if (!ChatClient.get().session.isConnected()) {
                            try {
                                ChatClient.get().connect();
                            } catch (InterruptedException ignored) {
                            }
                        }

                        String srv = (mc.isInSingleplayer() ? "local" : mc.getNetworkHandler().getServerInfo().address);
                        ChatClient.get().session.login(mc.getSession().getUsername(), srv);
                    }
                }
                return SINGLE_SUCCESS;
            }))
        );
    }

    public static class MessageHandler {
        @EventHandler
        public void onMessage(SendMessageEvent e) {
            e.cancel();
            String message = e.message;
            ChatClient.get().chat(message);
        }
    }
}
