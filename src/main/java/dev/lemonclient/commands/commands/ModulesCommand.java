package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ModulesCommand extends Command {
    public ModulesCommand() {
        super("modules", "Displays a list of all modules.", "features");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.info("--- Modules ((highlight)%d(default)) ---", Modules.get().getCount());

            Modules.loopCategories().forEach(category -> {
                MutableText categoryMessage = Text.literal("");
                Modules.get().getModulesByCategory(category).forEach(module -> categoryMessage.append(getModuleText(module)));
                ChatUtils.sendMsg(category.name, categoryMessage);
            });

            return SINGLE_SUCCESS;
        });
    }

    private MutableText getModuleText(Module module) {
        // Hover tooltip
        MutableText tooltip = Text.literal("");

        tooltip.append(Text.literal(module.title).formatted(Formatting.BLUE, Formatting.BOLD)).append("\n");
        tooltip.append(Text.literal(module.name).formatted(Formatting.GRAY)).append("\n\n");
        tooltip.append(Text.literal(module.description).formatted(Formatting.WHITE));

        MutableText finalModule = Text.literal(module.title);
        if (!module.isActive()) finalModule.formatted(Formatting.GRAY);
        if (!module.equals(Modules.get().getModulesByCategory(module.category).get(Modules.get().getModulesByCategory(module.category).size() - 1)))
            finalModule.append(Text.literal(", ").formatted(Formatting.GRAY));
        finalModule.setStyle(finalModule.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));

        return finalModule;
    }

}
