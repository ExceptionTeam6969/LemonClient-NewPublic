/*
package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.FakePlayerArgumentType;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.FakePlayer;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerEntity;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerManager;
import dev.lemonclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FakePlayerCommand extends Command {
    public FakePlayerCommand() {
        super("fake-player", "Manages fake players that you can use for testing.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
            .executes(context -> {
                FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
                FakePlayerManager.add(fakePlayer.name.get(), 20, fakePlayer.copyInventory.get());
                return SINGLE_SUCCESS;
            })
            .then(argument("name", StringArgumentType.word())
                .executes(context -> {
                    FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
                    FakePlayerManager.add(StringArgumentType.getString(context, "name"), 20, fakePlayer.copyInventory.get());
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("remove")
            .then(argument("fp", FakePlayerArgumentType.create())
                .executes(context -> {
                    FakePlayerEntity fp = FakePlayerArgumentType.get(context);
                    if (fp == null || !FakePlayerManager.contains(fp)) {
                        error("Couldn't find a Fake Player with that name.");
                        return SINGLE_SUCCESS;
                    }

                    FakePlayerManager.remove(fp);
                    info("Removed Fake Player %s.".formatted(fp.getEntityName()));

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("clear")
            .executes(context -> {
                FakePlayerManager.clear();
                return SINGLE_SUCCESS;
            })
        );

        builder.then(literal("list")
            .executes(context -> {
                info("--- Fake Players ((highlight)%s(default)) ---", FakePlayerManager.count());
                FakePlayerManager.forEach(fp -> ChatUtils.info("(highlight)%s".formatted(fp.getEntityName())));
                return SINGLE_SUCCESS;
            })
        );
    }
}
*/
