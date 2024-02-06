package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class Chat extends Module {
    public Chat() {
        super(Categories.Chat, "Chat", "Let you to connect to our chat server so you can chat with other users.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
        .name("Chat Prefix")
        .description("Message start with '@' is irc chat.")
        .defaultValue("@")
        .build()
    );

    public final Setting<Boolean> enable = sgGeneral.add(new BoolSetting.Builder()
        .name("Enable")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> enableTab = sgGeneral.add(new BoolSetting.Builder()
        .name("Enable Tab")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("The lemon user color.")
        .defaultValue(SettingColor.RED)
        .build()
    );

    public Text getPlayerName(PlayerListEntry playerListEntry) {
        Text name;
        name = playerListEntry.getDisplayName();
        if (name == null) name = Text.literal(playerListEntry.getProfile().getName());
        return name;
    }
}
