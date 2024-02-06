package dev.lemonclient.systems.modules.client;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import io.netty.buffer.Unpooled;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ServerSpoof extends Module {
    public ServerSpoof() {
        super(Categories.Misc, "Server Spoof", "Spoof client brand, resource pack and channels.");

        LemonClient.EVENT_BUS.subscribe(new Listener());
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> spoofBrand = sgGeneral.add(new BoolSetting.Builder()
        .name("spoof-brand")
        .description("Whether or not to spoof the brand.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> brand = sgGeneral.add(new StringSetting.Builder()
        .name("brand")
        .description("Specify the brand that will be send to the server.")
        .defaultValue("vanilla")
        .visible(spoofBrand::get)
        .build()
    );

    private final Setting<Boolean> resourcePack = sgGeneral.add(new BoolSetting.Builder()
        .name("resource-pack")
        .description("Spoof accepting server resource pack.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> blockChannels = sgGeneral.add(new BoolSetting.Builder()
        .name("block-channels")
        .description("Whether or not to block some channels.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> channels = sgGeneral.add(new StringListSetting.Builder()
        .name("channels")
        .description("If the channel contains the keyword, this outgoing channel will be blocked.")
        .defaultValue("minecraft:register")
        .visible(blockChannels::get)
        .build()
    );

    private class Listener {
        @EventHandler
        private void onPacketSend(PacketEvent.Send event) {
            if (!isActive()) return;
            if (event.packet instanceof CustomPayloadC2SPacket packet) {
                Identifier id = packet.getChannel();

                if (spoofBrand.get() && id.equals(CustomPayloadC2SPacket.BRAND))
                    packet.write(new PacketByteBuf(Unpooled.buffer()).writeString(brand.get()));

                if (blockChannels.get()) {
                    for (String channel : channels.get()) {
                        if (StringUtils.containsIgnoreCase(channel, id.toString())) {
                            event.cancel();
                            return;
                        }
                    }
                }
            }
        }

        @EventHandler
        private void onPacketRecieve(PacketEvent.Receive event) {
            if (!isActive()) return;

            if (resourcePack.get()) {
                if (!(event.packet instanceof ResourcePackSendS2CPacket packet)) return;
                event.cancel();
                MutableText msg = Text.literal("This server has ");
                msg.append(packet.isRequired() ? "a required " : "an optional ");
                MutableText link = Text.literal("resource pack");
                link.setStyle(link.getStyle()
                    .withColor(Formatting.BLUE)
                    .withUnderline(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.getURL()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to download")))
                );
                msg.append(link);
                msg.append(".");
                info(msg);
            }
        }
    }
}
