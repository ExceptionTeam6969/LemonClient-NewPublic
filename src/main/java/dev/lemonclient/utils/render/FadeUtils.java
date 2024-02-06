package dev.lemonclient.utils.render;

public class FadeUtils {
    protected long start;
    protected long length;

    public FadeUtils(long ms) {
        this.length = ms;
        this.reset();
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }

    public boolean isEnd() {
        return this.getTime() >= this.length;
    }

    public FadeUtils end() {
        this.start = System.currentTimeMillis() - this.length;
        return this;
    }

    protected long getTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return this.length;
    }

    private double getFadeOne() {
        return this.isEnd() ? 1.0 : (double) this.getTime() / (double) this.length;
    }

    public double getFade(FadeMode fadeMode) {
        return FadeUtils.getFade(fadeMode, this.getFadeOne());
    }

    public static double getFade(FadeMode fadeMode, double current) {
        switch (fadeMode) {
            case FADE_IN: {
                return FadeUtils.getFadeInDefault(current);
            }
            case FADE_OUT: {
                return FadeUtils.getFadeOutDefault(current);
            }
            case FADE_EPS_IN: {
                return FadeUtils.getEpsEzFadeIn(current);
            }
            case FADE_EPS_OUT: {
                return FadeUtils.getEpsEzFadeOut(current);
            }
            case FADE_EASE_IN_QUAD: {
                return FadeUtils.easeInQuad(current);
            }
            case FADE_EASE_OUT_QUAD: {
                return FadeUtils.easeOutQuad(current);
            }
        }
        return current;
    }

    public static double getFadeType(FadeType fadeType, boolean FadeIn, double current) {
        switch (fadeType) {
            case FADE_DEFAULT: {
                return FadeIn ? FadeUtils.getFadeInDefault(current) : FadeUtils.getFadeOutDefault(current);
            }
            case FADE_EPS: {
                return FadeIn ? FadeUtils.getEpsEzFadeIn(current) : FadeUtils.getEpsEzFadeOut(current);
            }
            case FADE_EASE_QUAD: {
                return FadeIn ? FadeUtils.easeInQuad(current) : FadeUtils.easeOutQuad(current);
            }
        }
        return FadeIn ? current : 1.0 - current;
    }

    private static double checkOne(double one) {
        return Math.max(0.0, Math.min(1.0, one));
    }

    public static double getFadeInDefault(double current) {
        return Math.tanh(FadeUtils.checkOne(current) * 3.0);
    }

    public static double getFadeOutDefault(double current) {
        return 1.0 - FadeUtils.getFadeInDefault(current);
    }

    public static double getEpsEzFadeIn(double current) {
        return 1.0 - FadeUtils.getEpsEzFadeOut(current);
    }

    public static double getEpsEzFadeOut(double current) {
        return Math.cos(1.5707963267948966 * FadeUtils.checkOne(current)) * Math.cos(2.5132741228718345 * FadeUtils.checkOne(current));
    }

    public static double easeOutQuad(double current) {
        return 1.0 - FadeUtils.easeInQuad(current);
    }

    public static double easeInQuad(double current) {
        return FadeUtils.checkOne(current) * FadeUtils.checkOne(current);
    }

    public enum FadeMode {
        FADE_IN,
        FADE_OUT,
        FADE_ONE,
        FADE_EPS_IN,
        FADE_EPS_OUT,
        FADE_EASE_OUT_QUAD,
        FADE_EASE_IN_QUAD

    }

    public enum FadeType {
        FADE_DEFAULT,
        FADE_ONE,
        FADE_EPS,
        FADE_EASE_QUAD

    }
}

