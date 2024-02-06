package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringSetting;
import dev.lemonclient.systems.hud.elements.ToastNotificationsHud;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.client.Notifications;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoLoadKit extends Module {
    public AutoLoadKit() {
        super(Categories.Chat, "Auto Load Kit", "Automatically takes specified kit after joining server/respawn.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> kCommand = sgGeneral.add(new StringSetting.Builder()
        .name("Kit Command")
        .description("Command to activate kit commands.")
        .defaultValue("/kit")
        .build()
    );
    private final Setting<String> kName = sgGeneral.add(new StringSetting.Builder()
        .name("Name Of Kit")
        .description("Name of kit that should be taken.")
        .defaultValue("2b2t")
        .build()
    );
    private final Setting<Notifications.Mode> notifications = sgGeneral.add(new EnumSetting.Builder<Notifications.Mode>()
        .name("Notifications")
        .defaultValue(Notifications.Mode.Toast)
        .build()
    );

    private boolean lock = false;
    private int i = 40;

    @EventHandler
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;
        lock = true;
        i = 40;
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        lock = true;
        i = 40;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (mc.currentScreen instanceof DeathScreen) return;
        if (lock) i--;
        if (lock && i <= 0) {
            switch (notifications.get()) {
                case Toast -> ToastNotificationsHud.addToast("Selected kit: " + kName.get());
                case Notification -> Managers.NOTIFICATION.info(title, "Selected kit: " + kName.get());
                case Chat -> info("Selected kit: " + kName.get());
            }
            ChatUtils.sendPlayerMsg(kCommand.get() + " " + kName.get());
            lock = false;
            i = 40;
        }
    }
}
