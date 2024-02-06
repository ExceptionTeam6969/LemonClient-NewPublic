package dev.lemonclient.utils.timers;

public final class MSTimer {

    private long time = -1L;

    private boolean loaded = false;

    public boolean hasTimePassed(final long MS) {
        return System.currentTimeMillis() >= time + MS;
    }

    public long hasTimeLeft(final long MS) {
        return (MS + time) - System.currentTimeMillis();
    }

    public void reset() {
        time = System.currentTimeMillis();
        loaded = true;
    }

    public void init() {
        if (!loaded) {
            reset();
        }
    }
}
