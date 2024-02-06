package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.PlayerArgumentType;
import dev.lemonclient.events.client.KeyEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SpectateCommand extends Command {

    private final StaticListener shiftListener = new StaticListener();

    public SpectateCommand() {
        super("spectate", "Allows you to spectate nearby players");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            LemonClient.mc.setCameraEntity(LemonClient.mc.player);
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            LemonClient.mc.setCameraEntity(PlayerArgumentType.get(context));
            LemonClient.mc.player.sendMessage(Text.literal("Sneak to un-spectate."), true);
            LemonClient.EVENT_BUS.subscribe(shiftListener);
            return SINGLE_SUCCESS;
        }));
    }

    private static class StaticListener {
        @EventHandler
        private void onKey(KeyEvent event) {
            if (LemonClient.mc.options.sneakKey.matchesKey(event.key, 0) || LemonClient.mc.options.sneakKey.matchesMouse(event.key)) {
                LemonClient.mc.setCameraEntity(LemonClient.mc.player);
                event.cancel();
                LemonClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}
