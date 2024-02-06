package dev.lemonclient.systems.modules.client;

import dev.lemonclient.events.client.MouseButtonEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringSetting;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.friends.Friend;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.misc.Placeholders;
import dev.lemonclient.utils.misc.input.KeyAction;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.render.ToastSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class MidClickFriend extends Module {
    public MidClickFriend() {
        super(Categories.Client, "Mid Click Friend", "Adds or removes a player as a friend using middle click.");
    }

    private final SettingGroup sgAdd = settings.createGroup("Add");
    private final SettingGroup sgRemove = settings.createGroup("Remove");

    // Add

    private final Setting<Boolean> friendAddMessage = sgAdd.add(new BoolSetting.Builder()
        .name("friend-add-message")
        .description("Sends a message to the player when you add them as a friend.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> friendAddMessageText = sgAdd.add(new StringSetting.Builder()
        .name("friend-add-message-text")
        .description("The message sent to the player after friending him.")
        .defaultValue("I just friended you on LemonClient!")
        .visible(friendAddMessage::get)
        .build()
    );

    // Remove

    private final Setting<Boolean> friendRemoveMessage = sgRemove.add(new BoolSetting.Builder()
        .name("friend-remove-message")
        .description("Sends a message to the player when you add them as a friend.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> friendRemoveMessageText = sgRemove.add(new StringSetting.Builder()
        .name("friend-remove-message-text")
        .description("The message sent to the player after unfriending him.")
        .defaultValue("I just unfriended you on LemonClient!")
        .visible(friendRemoveMessage::get)
        .build()
    );

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null && mc.targetedEntity != null && mc.targetedEntity instanceof PlayerEntity) {
            if (!Friends.get().isFriend((PlayerEntity) mc.targetedEntity)) {
                Friends.get().add(new Friend((PlayerEntity) mc.targetedEntity));
                if (friendAddMessage.get())
                    mc.player.networkHandler.sendChatMessage("/msg " + mc.targetedEntity.getEntityName() + " " + Placeholders.apply(friendAddMessageText.toString()));
                if (Config.get().chatFeedback.get())
                    ChatUtils.info("Friends", "Added (highlight)%s (default)to friends.", mc.targetedEntity.getEntityName());
                if (Config.get().toastFeedback.get())
                    mc.getToastManager().add(new ToastSystem(Items.EMERALD_BLOCK, Friends.get().color.getPacked(), "Friends " + Formatting.GRAY + "[" + Formatting.WHITE + mc.targetedEntity.getEntityName() + Formatting.GRAY + "]", null, Formatting.GRAY + "Added to friends.", Config.get().toastDuration.get()));
            } else {
                Friends.get().remove(Friends.get().get((PlayerEntity) mc.targetedEntity));
                if (friendRemoveMessage.get())
                    mc.player.networkHandler.sendChatMessage("/msg " + mc.targetedEntity.getEntityName() + " " + Placeholders.apply(friendRemoveMessageText.toString()));
                if (Config.get().chatFeedback.get())
                    ChatUtils.info("Friends", "Removed (highlight)%s (default)from friends.", mc.targetedEntity.getEntityName());
                if (Config.get().toastFeedback.get())
                    mc.getToastManager().add(new ToastSystem(Items.EMERALD_BLOCK, Friends.get().color.getPacked(), "Friends " + Formatting.GRAY + "[" + Formatting.WHITE + mc.targetedEntity.getEntityName() + Formatting.GRAY + "]", null, Formatting.GRAY + "Removed from friends.", Config.get().toastDuration.get()));
            }
        }
    }
}
