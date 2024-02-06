package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.ModuleArgumentType;
import dev.lemonclient.commands.arguments.SettingArgumentType;
import dev.lemonclient.commands.arguments.SettingValueArgumentType;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.gui.tabs.TabScreen;
import dev.lemonclient.gui.tabs.Tabs;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SettingCommand extends Command {
    public SettingCommand() {
        super("settings", "Allows you to view and change module settings.", "s");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
            literal("hud")
                .executes(context -> {
                    TabScreen screen = Tabs.get().get(3).createScreen(GuiThemes.get());
                    screen.parent = null;

                    Utils.screenToOpen = screen;
                    return SINGLE_SUCCESS;
                })
        );

        // Open module screen
        builder.then(
            argument("module", ModuleArgumentType.create())
                .executes(context -> {
                    Module module = context.getArgument("module", Module.class);

                    WidgetScreen screen = GuiThemes.get().moduleScreen(module);
                    screen.parent = null;

                    Utils.screenToOpen = screen;
                    return SINGLE_SUCCESS;
                })
        );

        // View or change settings
        builder.then(
            argument("module", ModuleArgumentType.create())
                .then(
                    argument("setting", SettingArgumentType.create())
                        .executes(context -> {
                            // Get setting value
                            Setting<?> setting = SettingArgumentType.get(context);

                            ModuleArgumentType.get(context).info("Setting (highlight)%s(default) is (highlight)%s(default).", setting.title, setting.get());

                            return SINGLE_SUCCESS;
                        })
                        .then(
                            argument("value", SettingValueArgumentType.create())
                                .executes(context -> {
                                    // Set setting value
                                    Setting<?> setting = SettingArgumentType.get(context);
                                    String value = SettingValueArgumentType.get(context);

                                    if (setting.parse(value)) {
                                        ModuleArgumentType.get(context).info("Setting (highlight)%s(default) changed to (highlight)%s(default).", setting.title, value);
                                    }

                                    return SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }
}
