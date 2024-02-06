package dev.lemonclient.systems.modules.movement.movementtimer.modes;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.movement.movementtimer.TimerMode;
import dev.lemonclient.systems.modules.movement.movementtimer.TimerModes;
import dev.lemonclient.systems.modules.world.Timer;
import dev.lemonclient.utils.player.PlayerUtils;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.systems.modules.movement.movementtimer.MovementTimer.*;

public class NCP extends TimerMode {
    public NCP() {
        super(TimerModes.NCP);
        timer = Modules.get().get(Timer.class);
    }

    private final Timer timer;

    @Override
    public void onDeactivate() {
        timer.setOverride(Timer.OFF);
    }

    @Override
    public void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (rechargeTimer == 0) {
            if (workingTimer > workingDelay) {
                rechargeTimer = rechargeDelay;
                workingTimer = 0;
                timer.setOverride(Timer.OFF);
            } else {
                if (settings.isActive()) {
                    if (settings.onlyInMove.get() && PlayerUtils.isMoving()) {
                        workingTimer++;
                        timer.setOverride(timerMultiplier);
                    } else if (!settings.onlyInMove.get()) {
                        workingTimer++;
                        timer.setOverride(timerMultiplier);
                    } else {
                        timer.setOverride(timerMultiplierOnRecharge);
                    }
                }
            }
        } else {
            rechargeTimer--;
            if (settings.isActive()) {
                timer.setOverride(timerMultiplierOnRecharge);
            } else {
                timer.setOverride(Timer.OFF);
            }
        }
    }
}
