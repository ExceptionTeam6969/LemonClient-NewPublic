package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IEntityVelocityUpdateS2CPacket;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class Velocity extends Module {
    public Velocity() {
        super(Categories.Movement, "Velocity", "Prevents you from being moved by external forces.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> knockback = sgGeneral.add(new BoolSetting.Builder()
        .name("knockback")
        .description("Modifies the amount of knockback you take from attacks.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> knockbackHorizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("knockback-horizontal")
        .description("How much horizontal knockback you will take.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(knockback::get)
        .build()
    );

    public final Setting<Double> knockbackVertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("knockback-vertical")
        .description("How much vertical knockback you will take.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(knockback::get)
        .build()
    );

    public final Setting<Boolean> explosions = sgGeneral.add(new BoolSetting.Builder()
        .name("explosions")
        .description("Modifies your knockback from explosions.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> explosionsHorizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("explosions-horizontal")
        .description("How much velocity you will take from explosions horizontally.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(explosions::get)
        .build()
    );

    public final Setting<Double> explosionsVertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("explosions-vertical")
        .description("How much velocity you will take from explosions vertically.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(explosions::get)
        .build()
    );

    public final Setting<Boolean> liquids = sgGeneral.add(new BoolSetting.Builder()
        .name("liquids")
        .description("Modifies the amount you are pushed by flowing liquids.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> liquidsHorizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("liquids-horizontal")
        .description("How much velocity you will take from liquids horizontally.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(liquids::get)
        .build()
    );

    public final Setting<Double> liquidsVertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("liquids-vertical")
        .description("How much velocity you will take from liquids vertically.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(liquids::get)
        .build()
    );

    public final Setting<Boolean> entityPush = sgGeneral.add(new BoolSetting.Builder()
        .name("entity-push")
        .description("Modifies the amount you are pushed by entities.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> entityPushAmount = sgGeneral.add(new DoubleSetting.Builder()
        .name("entity-push-amount")
        .description("How much you will be pushed.")
        .defaultValue(0)
        .sliderMax(1)
        .visible(entityPush::get)
        .build()
    );

    public final Setting<Boolean> blocks = sgGeneral.add(new BoolSetting.Builder()
        .name("blocks")
        .description("Prevents you from being pushed out of blocks.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> sinking = sgGeneral.add(new BoolSetting.Builder()
        .name("sinking")
        .description("Prevents you from sinking in liquids.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!sinking.get()) return;
        if (mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed()) return;

        if ((mc.player.isTouchingWater() || mc.player.isInLava()) && mc.player.getVelocity().y < 0) {
            ((IVec3d) mc.player.getVelocity()).setY(0);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (knockback.get() && event.packet instanceof EntityVelocityUpdateS2CPacket packet
            && ((EntityVelocityUpdateS2CPacket) event.packet).getId() == mc.player.getId()) {
            double velX = (packet.getVelocityX() / 8000d - mc.player.getVelocity().x) * knockbackHorizontal.get();
            double velY = (packet.getVelocityY() / 8000d - mc.player.getVelocity().y) * knockbackVertical.get();
            double velZ = (packet.getVelocityZ() / 8000d - mc.player.getVelocity().z) * knockbackHorizontal.get();
            ((IEntityVelocityUpdateS2CPacket) packet).setX((int) (velX * 8000 + mc.player.getVelocity().x * 8000));
            ((IEntityVelocityUpdateS2CPacket) packet).setY((int) (velY * 8000 + mc.player.getVelocity().y * 8000));
            ((IEntityVelocityUpdateS2CPacket) packet).setZ((int) (velZ * 8000 + mc.player.getVelocity().z * 8000));
        }
    }

    public double getHorizontal(Setting<Double> setting) {
        return isActive() ? setting.get() : 1;
    }

    public double getVertical(Setting<Double> setting) {
        return isActive() ? setting.get() : 1;
    }
}
