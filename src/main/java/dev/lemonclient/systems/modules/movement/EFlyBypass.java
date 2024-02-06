package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;

public class EFlyBypass extends Module {
    public EFlyBypass() {
        super(Categories.Movement, "EFly Bypass", "Elytra Fly that works on strict servers.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutomatic = settings.createGroup("Automatic");

    //----------General----------/
    private final Setting<Boolean> takeoff = sgGeneral.add(new BoolSetting.Builder().name("Take Off").defaultValue(false).build());
    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("Mode").defaultValue(Mode.Boost).build());
    private final Setting<Double> factor = sgGeneral.add(new DoubleSetting.Builder().name("Factor").defaultValue(1.3).min(0.1).sliderMax(4).build());

    //----------Automatic----------//
    private final Setting<Integer> startAt = sgAutomatic.add(new IntSetting.Builder().name("Start Fly At").description("The Y coordinate to start automatic module. Recommended 70~").defaultValue(65).min(25).sliderMin(25).sliderMax(255).build());
    private final Setting<Integer> targetY = sgAutomatic.add(new IntSetting.Builder().name("Target Y").description("The target coordinate for boosting.").defaultValue(100).min(50).sliderMin(50).sliderMax(255).build());
    private final Setting<Integer> boostingTicks = sgAutomatic.add(new IntSetting.Builder().name("BoostingTicks").description("Amount of ticks to boost").defaultValue(20).min(20).max(80).sliderMin(20).sliderMax(80).build());

    boolean boosting;
    int boostTicks;

    @Override
    public void onActivate() {
        boosting = false;
        boostTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        if (!mc.player.isFallFlying()) {
            if (takeoff.get() && !mc.player.isOnGround() && mc.options.jumpKey.isPressed())
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return;
        }

        if (mc.player.getAbilities().flying) {
            mc.player.getAbilities().flying = false;
        }

        //Boost
        if (mode.get() == Mode.Boost) {
            if (mc.options.forwardKey.isPressed()) {
                mc.player.addVelocity(-MathHelper.sin(yaw) * factor.get() / 10, 0, MathHelper.cos(yaw) * factor.get() / 10);
            }
        }

        if (mode.get() == Mode.Automatic) {
            int y = (int) mc.player.getY();
            int b = boostingTicks.get() / 4;


            if (boosting || y >= startAt.get() && y < targetY.get()) {
                boosting = true;
                boostTicks++;


                if (boostTicks < b * 2) {
                    mc.player.setPitch(35);
                    mc.player.addVelocity(-MathHelper.sin(yaw) * factor.get() / 10, 0, MathHelper.cos(yaw) * factor.get() / 10);
                } else {
                    float i = boostTicks / 1.5f;
                    if (i > 35) i = 35;
                    mc.player.setPitch(-i);
                }

                if (boostTicks > boostingTicks.get() + 10) {
                    boosting = false;
                    boostTicks = 0;
                }
            }
        }
    }

    public enum Mode {
        Boost,
        Automatic
    }
}
