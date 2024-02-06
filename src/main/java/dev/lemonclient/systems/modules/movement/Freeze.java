package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.entity.EntityRemovedEvent;
import dev.lemonclient.events.entity.player.InteractBlockEvent;
import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Freeze extends Module {
    public Freeze() {
        super(Categories.Movement, "Freeze", "Freezes your position. (1.8: noc03)");
    }

    private final SettingGroup FSettings = settings.createGroup("Freeze Settings");

    private final Setting<Boolean> freezeLook = FSettings.add(new BoolSetting.Builder()
        .name("Freeze Look")
        .description("Freezes your pitch and yaw.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> packet = FSettings.add(new BoolSetting.Builder()
        .name("Packet")
        .description("Enable packet mode, better.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> freezeLookSilent = FSettings.add(new BoolSetting.Builder()
        .name("Freeze Look Silent")
        .description("Freezes your pitch and yaw silent.")
        .defaultValue(true)
        .visible(() -> packet.get() && freezeLook.get())
        .build()
    );

    private final Setting<Boolean> freezeLookPlace = FSettings.add(new BoolSetting.Builder()
        .name("Freeze Look Place Support")
        .description("Unfreez you yaw and pitch on place")
        .defaultValue(false)
        .visible(freezeLookSilent::get)
        .build()
    );

    private float yaw = 0;
    private float pitch = 0;
    private Vec3d position = Vec3d.ZERO;

    @Override()
    public void onActivate() {
        if (mc.player != null) {
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
            position = mc.player.getPos();
        }
    }

    private boolean rotate = false;

    private void setFreezeLook(PacketEvent.Send event, PlayerMoveC2SPacket playerMove) {
        if (playerMove.changesLook() && freezeLook.get() && freezeLookSilent.get() && !rotate) {
            event.setCancelled(true);
        } else if (mc.player != null && playerMove.changesLook() && freezeLook.get() && !freezeLookSilent.get()) {
            event.setCancelled(true);
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
        if (mc.player != null && playerMove.changesPosition()) {
            mc.player.setVelocity(0, 0, 0);
            mc.player.setPos(position.x, position.y, position.z);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (mc.player != null && mc.getNetworkHandler() != null && freezeLookPlace.get()) {
            PlayerMoveC2SPacket.LookAndOnGround r = new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround());
            rotate = true;
            mc.getNetworkHandler().sendPacket(r);
            rotate = false;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket playerMove) {
            if (packet.get()) {
                setFreezeLook(event, playerMove);
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null) {
            mc.player.setVelocity(0, 0, 0);
            mc.player.setPos(position.x, position.y, position.z);
        }
    }

    @EventHandler
    private void remove(EntityRemovedEvent event) {
        if (event.entity == mc.player) {
            if (isActive()) {
                toggle();
            }
        }
    }
}
