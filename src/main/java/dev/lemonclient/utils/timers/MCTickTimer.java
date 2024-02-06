package dev.lemonclient.utils.timers;

public class MCTickTimer {
    int tick = 0;

    public void update() {
        tick++;
    }

    public void reset() {
        tick = 0;
    }

    public boolean hasTimePassed(int ticks) {
        return tick >= ticks;
    }
}
