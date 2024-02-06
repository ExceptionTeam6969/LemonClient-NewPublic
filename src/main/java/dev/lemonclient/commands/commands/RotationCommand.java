package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.DirectionArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class RotationCommand extends Command {
    public RotationCommand() {
        super("rotation", "Modifies your rotation.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
            .then(literal("set")
                .then(argument("direction", DirectionArgumentType.create())
                    .executes(context -> {
                        LemonClient.mc.player.setPitch(context.getArgument("direction", Direction.class).getVector().getY() * -90);
                        LemonClient.mc.player.setYaw(context.getArgument("direction", Direction.class).asRotation());

                        return SINGLE_SUCCESS;
                    }))
                .then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
                    .executes(context -> {
                        LemonClient.mc.player.setPitch(context.getArgument("pitch", Float.class));

                        return SINGLE_SUCCESS;
                    })
                    .then(argument("yaw", FloatArgumentType.floatArg(-180, 180))
                        .executes(context -> {
                            LemonClient.mc.player.setPitch(context.getArgument("pitch", Float.class));
                            LemonClient.mc.player.setYaw(context.getArgument("yaw", Float.class));

                            return SINGLE_SUCCESS;
                        })
                    )
                )
            )
            .then(literal("add")
                .then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
                    .executes(context -> {
                        float pitch = LemonClient.mc.player.getPitch() + context.getArgument("pitch", Float.class);
                        LemonClient.mc.player.setPitch(pitch >= 0 ? Math.min(pitch, 90) : Math.max(pitch, -90));

                        return SINGLE_SUCCESS;
                    })
                    .then(argument("yaw", FloatArgumentType.floatArg(-180, 180))
                        .executes(context -> {
                            float pitch = LemonClient.mc.player.getPitch() + context.getArgument("pitch", Float.class);
                            LemonClient.mc.player.setPitch(pitch >= 0 ? Math.min(pitch, 90) : Math.max(pitch, -90));

                            float yaw = LemonClient.mc.player.getYaw() + context.getArgument("yaw", Float.class);
                            LemonClient.mc.player.setYaw(MathHelper.wrapDegrees(yaw));

                            return SINGLE_SUCCESS;
                        })
                    )
                )
            );
    }
}
