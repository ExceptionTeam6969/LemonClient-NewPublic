package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.bot.BotNet;
import dev.lemonclient.bot.actions.BotCmdAction;
import dev.lemonclient.bot.actions.SayAction;
import dev.lemonclient.bot.client.BotClient;
import dev.lemonclient.bot.server.BotServer;
import dev.lemonclient.commands.Command;
import dev.lemonclient.systems.modules.client.BotNetModule;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BotNetCommand extends Command {
    public BotNetCommand() {
        super("bot", "Bots create", "botnet");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("connect").executes(context -> {
            try {
                BotNetModule.SockMode mode = BotNetModule.getMode();
                switch (mode) {
                    case Client ->
                        BotNet.createClient(BotNetModule.getConnectHost(), BotNetModule.getConnectPort()).connect();
                    case Server -> BotNet.createServer().launch(BotNetModule.getServerPort());
                }
            } catch (Exception e) {
                error(e.getMessage());
            }


            return SINGLE_SUCCESS;
        }));

        builder.then(literal("disconnect").executes(context -> {
            try {
                if (BotClient.INSTANCE != null) {
                    BotClient.INSTANCE.stop();
                }
                if (BotServer.INSTANCE != null) {
                    BotServer.INSTANCE.stop();
                }
            } catch (Exception e) {
                error(e.getMessage());
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("chat").then(argument("message", StringArgumentType.string()).executes(context -> {
            String msg = StringArgumentType.getString(context, "message");
            if (BotClient.INSTANCE != null) {
                if (BotClient.INSTANCE.session != null) {
                    BotClient.INSTANCE.session.send(new SayAction(msg));
                }
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("botcmd").then(argument("cmd", StringArgumentType.string()).executes(context -> {
            try {
                String msg = StringArgumentType.getString(context, "cmd");
                if (BotClient.INSTANCE != null) {
                    if (BotClient.INSTANCE.session != null) {
                        BotClient.INSTANCE.session.send(new BotCmdAction(msg));
                    }
                }
            } catch (Exception e) {
                error(e.getMessage());
            }
            return SINGLE_SUCCESS;
        })));
    }
}
