package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PeekCommand extends Command {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    private static final SimpleCommandExceptionType NOT_HOLDING_SHULKER_BOX = new SimpleCommandExceptionType(Text.literal("You must be holding a storage block with items in it."));

    public PeekCommand() {
        super("peek", "Lets you see what's inside storage block items.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (Utils.openContainer(LemonClient.mc.player.getMainHandStack(), ITEMS, true)) return SINGLE_SUCCESS;
            else if (Utils.openContainer(LemonClient.mc.player.getOffHandStack(), ITEMS, true)) return SINGLE_SUCCESS;
            else throw NOT_HOLDING_SHULKER_BOX.create();
        });
    }
}
