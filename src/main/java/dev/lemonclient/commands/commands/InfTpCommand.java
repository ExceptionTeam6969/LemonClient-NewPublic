package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.ClientPosArgumentType;
import dev.lemonclient.commands.arguments.PlayerArgumentType;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.InfiniteAura;
import dev.lemonclient.utils.path.TeleportPath;
import dev.lemonclient.utils.timers.MCTickTimer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.lemonclient.LemonClient.mc;

public class InfTpCommand extends Command {
    private Vec3d to = null;
    private List<Vec3d> path = new CopyOnWriteArrayList<>();
    MCTickTimer timer = new MCTickTimer();

    public InfTpCommand() {
        super("infTp", "fast teleport", "itp", "inf");
        LemonClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("player").then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.get(context);
            Vec3d topPlayer = mc.player.getPos();
            to = player.getPos();
            TeleportPath.teleport(topPlayer, to);
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("pos").then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Vec3d to = ClientPosArgumentType.getPos(ctx, "pos");
            TeleportPath.teleportTo(to);
            return SINGLE_SUCCESS;
        })));
    }

    @EventHandler
    public void onUpdate(TickEvent.Pre e) {
        if (path != null) {
            if (timer.hasTimePassed(15)) {
                path = null;
                timer.reset();
            }
            timer.update();
        }
    }

    @EventHandler
    public void render(Render3DEvent event) {
        if (path != null) {
            Modules.get().get(InfiniteAura.class).renderPath(event, path);
        }
    }
}
