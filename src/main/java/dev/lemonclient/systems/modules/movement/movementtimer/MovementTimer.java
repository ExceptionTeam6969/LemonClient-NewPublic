package dev.lemonclient.systems.modules.movement.movementtimer;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.movement.movementtimer.modes.NCP;
import dev.lemonclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

public class MovementTimer extends Module {
    public MovementTimer() {
        super(Categories.Movement, "Movement Timer", "Bypass timer.");

        autoSubscribe = false;
        LemonClient.EVENT_BUS.subscribe(this);
    }

    public static int workingDelay = 27;
    public static int workingTimer = 0;
    public static int rechargeTimer = 0; // Reset timer
    public static int rechargeDelay = 352; // Recharge delay
    public static double timerMultiplier = 2; // Timer multiplier
    public static double timerMultiplierOnRecharge = 1; // Timer multiplier on recharge

    private final SettingGroup settingsGroup = settings.getDefaultGroup();

    public final Setting<Boolean> onlyInMove = settingsGroup.add(new BoolSetting.Builder()
        .name("work-only-in-move")
        .description("Prevent false un charge.")
        .defaultValue(true)
        .build()
    );
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<TimerModes> mode = sgGeneral.add(new EnumSetting.Builder<TimerModes>()
        .name("mode")
        .description("Timer mode.")
        .defaultValue(TimerModes.NCP)
        .onModuleActivated(timerModesSetting -> onTimerModeChanged(timerModesSetting.get()))
        .onChanged(this::onTimerModeChanged)
        .build()
    );

    private final Setting<Boolean> rechargeOnDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("recharge-on-disable")
        .description("Recharge timer delay on disable.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> rechargeDelaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("recharge-delay")
        .description("Recharge timer delay.")
        .defaultValue(352)
        .visible(() -> mode.get() == TimerModes.Custom)
        .onChanged((a) -> {
            rechargeDelay = a;
            rechargeTimer = 0;
        })
        .build()
    );

    private final Setting<Integer> boostDelaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("boost-delay")
        .description("Working timer delay.")
        .defaultValue(27)
        .visible(() -> mode.get() == TimerModes.Custom)
        .onChanged((a) -> {
            workingDelay = a;
            workingTimer = 0;
        })
        .build()
    );

    private final Setting<Double> boostMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("multiplier")
        .description("Timer multiplier.")
        .defaultValue(2)
        .visible(() -> mode.get() == TimerModes.Custom)
        .onChanged((a) -> {
            timerMultiplier = a;
        })
        .build()
    );

    private final Setting<Double> boostMultiplierOnRecharge = sgGeneral.add(new DoubleSetting.Builder()
        .name("multiplier-on-recharge")
        .description("Timer multiplier on recharge.")
        .defaultValue(1)
        .visible(() -> mode.get() == TimerModes.Custom)
        .onChanged((a) -> {
            timerMultiplierOnRecharge = a;
        })
        .build()
    );

    private TimerMode currentMode;

    private void onTimerModeChanged(TimerModes mode) {
        switch (mode) {
            case NCP -> {
                currentMode = new NCP();
                workingDelay = 27;
                rechargeDelay = 352;
                timerMultiplier = 2;
                timerMultiplierOnRecharge = Timer.OFF;
            }
            case Intave -> {
                currentMode = new NCP();
                workingDelay = 30;
                rechargeDelay = 105;
                timerMultiplier = 1.25;
                timerMultiplierOnRecharge = Timer.OFF;
            }
            case Custom -> {
                currentMode = new NCP();
                workingDelay = boostDelaySetting.get();
                rechargeDelay = rechargeDelaySetting.get();
                timerMultiplier = boostMultiplier.get();
                timerMultiplierOnRecharge = boostMultiplierOnRecharge.get();
            }
        }
    }

    @Override
    public void onActivate() {
        currentMode.onActivate();
    }

    @Override
    public void onDeactivate() {
        if (rechargeOnDisable.get()) {
            workingTimer = 0;
            rechargeTimer = 0;
        }
        currentMode.onDeactivate();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        currentMode.onPreTick(event);
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        currentMode.onPostTick(event);
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        currentMode.onSendPacket(event);
    }

    @EventHandler
    public void onSentPacket(PacketEvent.Sent event) {
        currentMode.onSentPacket(event);
    }
}

