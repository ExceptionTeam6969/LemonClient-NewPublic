package dev.lemonclient.utils.timers;

public class MeteorTimerUtils {
    boolean paused;
    private long time = -1L;
    private long currentTime = this.getCurrentTime();

    protected final long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void reset() {
        this.time = System.nanoTime();
    }

    public void resetCurrent() {
        this.currentTime = System.currentTimeMillis();
    }

    public long convertMicroToNS(long time) {
        return time * 1000L;
    }

    public long convertMillisToMicro(long time) {
        return time * 1000L;
    }

    public long convertTicksToMillis(long ticks) {
        return ticks * 50;
    }

    public boolean passedS(double s) {
        return this.passedMs((long) s * 1000L);
    }

    public boolean passedDms(double dms) {
        return this.passedMs((long) dms * 10L);
    }

    public boolean passedDs(double ds) {
        return this.passedMs((long) ds * 100L);
    }

    public boolean passedMs(long ms) {
        return !this.paused && this.passedNS(this.convertToNS(ms));
    }

    public boolean passedTicks(long ticks) {
        return passedNS(convertTicksToNS(ticks));
    }

    public void setMs(long ms) {
        this.time = System.nanoTime() - this.convertToNS(ms);
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - this.time >= ns;
    }

    public long getPassedTimeMs() {
        return this.getMs(System.nanoTime() - this.time);
    }

    public final boolean passed(long delay) {
        return this.passed(delay, false);
    }

    public boolean passed(long delay, boolean reset) {
        if (reset) {
            this.reset();
        }
        return System.currentTimeMillis() - this.time >= delay;
    }

    public boolean passedCurrent(long ms) {
        return System.currentTimeMillis() - this.currentTime >= ms;
    }

    public boolean passedCurrent(double ms) {
        return (System.currentTimeMillis() - this.time) >= ms;
    }

    public long getMs(long time) {
        return time / 1000000L;
    }

    public long convertToNS(long time) {
        return time * 1000000L;
    }

    public long convertTicksToNS(long ticks) {
        return convertMillisToNS(convertTicksToMillis(ticks));
    }

    public long convertMillisToNS(long time) {
        return convertMicroToNS(convertMillisToMicro(time));
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
