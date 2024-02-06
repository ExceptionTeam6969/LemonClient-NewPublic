package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameMode;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "Changes your gamemode client-side.", "gm");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        for (GameMode gameMode : GameMode.values()) {
            builder.then(literal(gameMode.getName()).executes(context -> {
                LemonClient.mc.interactionManager.setGameMode(gameMode);
                return SINGLE_SUCCESS;
            }));
        }
    }
}
