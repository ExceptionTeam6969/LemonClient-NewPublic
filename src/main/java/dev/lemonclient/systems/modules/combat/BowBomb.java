package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.timers.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

public class BowBomb extends Module {
    public BowBomb() {
        super(Categories.Combat, "Bow Bomb", "One tapping by using bow exploit.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSelection = settings.createGroup("Selection");

    //--------------------General--------------------//
    private final Setting<Boolean> rotation = boolSetting(sgGeneral, "Rotation", false);
    private final Setting<ModeEn> Mode = enumSetting(sgGeneral, "Mode", ModeEn.Maximum);
    private final Setting<Double> factor = doubleSetting(sgGeneral, "Factor", 1.0, 1.0, 20.0);
    private final Setting<ExploitEn> exploit = enumSetting(sgGeneral, "Exploit", ExploitEn.Strong);
    private final Setting<Boolean> minimize = boolSetting(sgGeneral, "Minimize", false);
    private final Setting<Double> delay = doubleSetting(sgGeneral, "Delay", 5.0, 0.0, 10.0);

    //--------------------Selection--------------------//
    private final Setting<Boolean> bow = boolSetting(sgSelection, "Bows", true);
    private final Setting<Boolean> pearls = boolSetting(sgSelection, "EPearls", true);
    private final Setting<Boolean> xp = boolSetting(sgSelection, "XP", true);
    private final Setting<Boolean> eggs = boolSetting(sgSelection, "Eggs", true);
    private final Setting<Boolean> potions = boolSetting(sgSelection, "SplashPotions", true);
    private final Setting<Boolean> snowballs = boolSetting(sgSelection, "Snowballs", true);

    private final Random rnd = new Random();
    public static TimerUtils delayTimer = new TimerUtils();

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (!Utils.canUpdate() || !delayTimer.passedMs(delay.get().longValue() * 1000L)) return;

        if (event.packet instanceof PlayerActionC2SPacket && ((PlayerActionC2SPacket) event.packet).getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && (mc.player.getActiveItem().getItem() == Items.BOW && bow.get())
            || event.packet instanceof PlayerInteractItemC2SPacket && ((PlayerInteractItemC2SPacket) event.packet).getHand() == Hand.MAIN_HAND && ((mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL && pearls.get()) || (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE && xp.get()) || (mc.player.getMainHandStack().getItem() == Items.EGG && eggs.get()) || (mc.player.getMainHandStack().getItem() == Items.SPLASH_POTION && potions.get()) || (mc.player.getMainHandStack().getItem() == Items.SNOWBALL && snowballs.get()))) {

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

            double[] strict_direction = new double[]{100f * -Math.sin(Math.toRadians(mc.player.getYaw())), 100f * Math.cos(Math.toRadians(mc.player.getYaw()))};

            if (exploit.get() == ExploitEn.Fast) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), minimize.get() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                }
            }
            if (exploit.get() == ExploitEn.Strong) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                    spoof(mc.player.getX(), minimize.get() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                }
            }
            if (exploit.get() == ExploitEn.Phobos) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000013, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000027, mc.player.getZ(), false);
                }
            }
            if (exploit.get() == ExploitEn.Strict) {
                for (int i = 0; i < getRuns(); i++) {
                    if (rnd.nextBoolean()) {
                        spoof(mc.player.getX() - strict_direction[0], mc.player.getY(), mc.player.getZ() - strict_direction[1], false);
                    } else {
                        spoof(mc.player.getX() + strict_direction[0], mc.player.getY(), mc.player.getZ() + strict_direction[1], true);
                    }
                }
            }
            if (exploit.get() == ExploitEn.WB) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX() + getWorldBorderRnd(), mc.player.getY(), mc.player.getZ() + getWorldBorderRnd(), false);
                }
            }
            delayTimer.reset();
        }
    }

    private void spoof(double x, double y, double z, boolean ground) {
        if (rotation.get()) {
            sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
        } else {
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
        }
    }

    private int getRuns() {
        if (Mode.get() == ModeEn.Factorised) {
            return 10 + (int) ((factor.get() - 1));
        }
        if (Mode.get() == ModeEn.Normal) {
            return (int) Math.floor(factor.get());
        }
        if (Mode.get() == ModeEn.Maximum) {
            return (int) (30f * factor.get());
        }
        return 1;
    }

    private int getWorldBorderRnd() {
        if (mc.isInSingleplayer()) {
            return 1;
        }
        int n = rnd.nextInt(29000000);
        if (rnd.nextBoolean()) {
            return n;
        }
        return -n;
    }

    public enum ExploitEn {
        Strong,
        Fast,
        Strict,
        Phobos,
        WB
    }

    public enum ModeEn {
        Normal,
        Maximum,
        Factorised
    }
}
