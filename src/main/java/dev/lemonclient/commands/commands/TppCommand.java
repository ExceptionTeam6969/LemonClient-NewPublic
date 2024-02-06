package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.PlayerArgumentType;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.managers.Managers;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Random;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.lemonclient.LemonClient.mc;

public class TppCommand extends Command {
    public TppCommand() {
        super("tpplayer", ".tp player");
    }

    private boolean cameraset = false;
    private boolean bp = false;
    private double upblocks = 0.0;
    private double xaddblocks = 0.0;
    private double zaddblocks = 0.0;

    private double bblock = 3.0;
    private double bY = 0.0;
    private double rY = 1.0;

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.get(context);
            if (cameraset) {
                Managers.ROTATION.start(player.getBoundingBox(), 1000, RotationType.Other, Objects.hash(getName() + "other"));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + xaddblocks, player.getY() + upblocks, player.getZ() + zaddblocks, player.isOnGround()));
                mc.player.setPos(player.getX() + xaddblocks, player.getY() + upblocks, player.getZ() + zaddblocks);
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + xaddblocks, player.getY() + upblocks, player.getZ() + zaddblocks, player.isOnGround()));
                mc.player.setPos(player.getX() + xaddblocks, player.getY() + upblocks, player.getZ() + zaddblocks);
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("setAddX")
            .then(argument("addX", DoubleArgumentType.doubleArg()).executes(context -> {
                this.xaddblocks = context.getArgument("addX", Double.class);
                return SINGLE_SUCCESS;
            })));
        builder.then(literal("setAddY")
            .then(argument("addY", DoubleArgumentType.doubleArg()).executes(context -> {
                this.upblocks = context.getArgument("addY", Double.class);
                return SINGLE_SUCCESS;
            })));
        builder.then(literal("setAddZ")
            .then(argument("addZ", DoubleArgumentType.doubleArg()).executes(context -> {
                this.zaddblocks = context.getArgument("addZ", Double.class);
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("behind")
            .then(argument("player", PlayerArgumentType.create()).executes(context -> {
                ClientPlayerEntity thePlayer = mc.player;
                assert thePlayer != null;

                PlayerEntity targetPlayer = PlayerArgumentType.get(context);


                Vec3d forward;

                if (bp) {
                    forward = Vec3d.fromPolar(targetPlayer.getPitch(), targetPlayer.getYaw()).normalize();
                } else {
                    forward = Vec3d.fromPolar(0, targetPlayer.getYaw()).normalize();
                }

                thePlayer.setPosition(targetPlayer.getX() + forward.x * bblock, targetPlayer.getY() + bY, targetPlayer.getZ() + forward.z * bblock);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetPlayer.getX() + forward.x * bblock, targetPlayer.getY() + bY, targetPlayer.getZ() + forward.z * bblock, targetPlayer.isOnGround()));
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("behindblock")
            .then(argument("block", DoubleArgumentType.doubleArg()).executes(context -> {
                this.bblock = context.getArgument("block", Double.class);
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("behindY")
            .then(argument("Y", DoubleArgumentType.doubleArg()).executes(context -> {
                this.bY = context.getArgument("Y", Double.class);
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("behindPitchOn").executes(context -> {
            this.bp = true;
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("behindPitchOff").executes(context -> {
            this.bp = false;
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("followCameraOff").executes(context -> {
            this.cameraset = true;
            return SINGLE_SUCCESS;
        }));


        builder.then(literal("followCameraOn").executes(context -> {
            this.cameraset = false;
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("rtp")
            .then(argument("player", PlayerArgumentType.create()).executes(context -> {
                ClientPlayerEntity thePlayer = mc.player;
                assert thePlayer != null;

                PlayerEntity targetPlayer = PlayerArgumentType.get(context);

                thePlayer.setPosition(targetPlayer.getX() + new Random().nextInt(3) * 2 - 2, targetPlayer.getY() + rY, targetPlayer.getZ() + new Random().nextInt(3) * 2 - 2);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetPlayer.getX() + new Random().nextInt(3) * 2 - 2, targetPlayer.getY() + rY, targetPlayer.getZ() + new Random().nextInt(3) * 2 - 2, targetPlayer.isOnGround()));
                return SINGLE_SUCCESS;
            })));

        builder.then(literal("rtpY")
            .then(argument("Y", DoubleArgumentType.doubleArg()).executes(context -> {
                this.rY = context.getArgument("Y", Double.class);
                return SINGLE_SUCCESS;
            })));
    }
}
