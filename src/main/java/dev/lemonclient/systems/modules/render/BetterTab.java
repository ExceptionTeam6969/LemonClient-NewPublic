package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.friends.Friend;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class BetterTab extends Module {
    public BetterTab() {
        super(Categories.Render, "Better Tab", "Various improvements to the tab list.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> tabSize = sgGeneral.add(new IntSetting.Builder()
        .name("tablist-size")
        .description("How many players in total to display in the tablist.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 1000)
        .build()
    );

    public final Setting<Integer> tabHeight = sgGeneral.add(new IntSetting.Builder()
        .name("column-height")
        .description("How many players to display in each column.")
        .defaultValue(20)
        .min(1)
        .sliderRange(1, 1000)
        .build()
    );

    private final Setting<Boolean> self = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-self")
        .description("Highlights yourself in the tablist.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> selfColor = sgGeneral.add(new ColorSetting.Builder()
        .name("self-color")
        .description("The color to highlight your name with.")
        .defaultValue(new SettingColor(250, 130, 30))
        .visible(self::get)
        .build()
    );

    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-friends")
        .description("Highlights friends in the tablist.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> accurateLatency = sgGeneral.add(new BoolSetting.Builder()
        .name("accurate-latency")
        .description("Shows latency as a number in the tablist.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> gamemode = sgGeneral.add(new BoolSetting.Builder()
        .name("gamemode")
        .description("Display gamemode next to the nick.")
        .defaultValue(false)
        .build()
    );

    public Text getPlayerName(PlayerListEntry playerListEntry) {
        Text name;
        Color color = null;

        name = playerListEntry.getDisplayName();
        if (name == null) name = Text.literal(playerListEntry.getProfile().getName());

        if (playerListEntry.getProfile().getId().toString().equals(mc.player.getGameProfile().getId().toString()) && self.get()) {
            color = selfColor.get();
        } else if (friends.get() && Friends.get().isFriend(playerListEntry)) {
            Friend friend = Friends.get().get(playerListEntry);
            if (friend != null) color = Config.get().friendColor.get();
        }

        if (color != null) {
            String nameString = name.getString();

            for (Formatting format : Formatting.values()) {
                if (format.isColor()) nameString = nameString.replace(format.toString(), "");
            }

            name = Text.literal(nameString).setStyle(name.getStyle().withColor(TextColor.fromRgb(color.getPacked())));
        }

        if (gamemode.get()) {
            GameMode gm = playerListEntry.getGameMode();
            String gmText = "?";
            if (gm != null) {
                gmText = switch (gm) {
                    case SPECTATOR -> "Sp";
                    case SURVIVAL -> "S";
                    case CREATIVE -> "C";
                    case ADVENTURE -> "A";
                };
            }
            MutableText text = Text.literal("");
            text.append(name);
            text.append(" [" + gmText + "]");
            name = text;
        }

        return name;
    }

}
