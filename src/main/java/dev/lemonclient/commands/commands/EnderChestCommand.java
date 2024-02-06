package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class EnderChestCommand extends Command {
    public EnderChestCommand() {
        super("ender-chest", "Allows you to preview memory of your ender chest.", "ec", "echest");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Utils.openContainer(Items.ENDER_CHEST.getDefaultStack(), new ItemStack[27], true);
            return SINGLE_SUCCESS;
        });
    }
}
