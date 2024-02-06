package dev.lemonclient.systems.modules.misc;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.world.ServerConnectBeginEvent;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoReconnect extends Module {
    public AutoReconnect() {
        super(Categories.Misc, "Auto Reconnect", "Automatically reconnects when disconnected from a server.");

        LemonClient.EVENT_BUS.subscribe(new StaticListener());
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> time = sgGeneral.add(new DoubleSetting.Builder()
        .name("delay")
        .description("The amount of seconds to wait before reconnecting to the server.")
        .defaultValue(3.5)
        .min(0)
        .decimalPlaces(1)
        .build()
    );

    public Pair<ServerAddress, ServerInfo> lastServerConnection;

    private class StaticListener {
        @EventHandler
        private void onGameJoined(ServerConnectBeginEvent event) {
            lastServerConnection = new ObjectObjectImmutablePair<>(event.address, event.info);
        }
    }
}
