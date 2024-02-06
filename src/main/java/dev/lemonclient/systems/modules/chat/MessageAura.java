package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.entity.EntityAddedEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringSetting;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class MessageAura extends Module {
    public MessageAura() {
        super(Categories.Chat, "Message Aura", "Sends a specified message to any player that enters render distance.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
        .name("Message")
        .description("The specified message sent to the player.")
        .defaultValue("I saw you on LemonClient!")
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description("Will not send any messages to people friended.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity) || event.entity.getUuid().equals(mc.player.getUuid())) return;

        if (!ignoreFriends.get() || (ignoreFriends.get() && !Friends.get().isFriend((PlayerEntity) event.entity))) {
            ChatUtils.sendPlayerMsg("/msg " + event.entity.getEntityName() + " " + message.get());
        }
    }
}
