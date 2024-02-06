package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.game.SendMessageEvent;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringListSetting;
import dev.lemonclient.settings.StringSetting;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.util.List;

public class GroupChat extends Module {
    public GroupChat() {
        super(Categories.Chat, "Group Chat", "Talks with people in groups privately using /msg.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> players = sgGeneral.add(new StringListSetting.Builder()
        .name("players")
        .description("Determines which players to message.")
        .defaultValue(
            "Fin_LemonKee",
            "_ImWuMie"
        )
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("How the message command is set up on the server.")
        .defaultValue("/msg %player% %message%")
        .build()
    );

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        for (String playerString : players.get()) {
            for (PlayerListEntry onlinePlayer : mc.getNetworkHandler().getPlayerList()) {
                if (onlinePlayer.getProfile().getName().equalsIgnoreCase(playerString)) {
                    ChatUtils.sendPlayerMsg(command.get().replace("%player%", onlinePlayer.getProfile().getName()).replace("%message%", event.message));
                    break;
                }
            }
        }

        event.cancel();
    }
}
