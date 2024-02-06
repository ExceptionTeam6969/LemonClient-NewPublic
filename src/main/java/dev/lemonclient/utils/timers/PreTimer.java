package dev.lemonclient.utils.timers;

public class PreTimer {
    private long previousTime = -1L;

    public PreTimer() {
    }

    public boolean check(float milliseconds) {
        return (float) this.getTime() >= milliseconds;
    }

    public long getTime() {
        return this.getCurrentTime() - this.previousTime;
    }

    public void reset() {
        this.previousTime = this.getCurrentTime();
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
