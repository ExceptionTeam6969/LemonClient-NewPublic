package dev.lemonclient.clion.animations.plus;

public interface EasingExpend {
    /**
     * An EasingIn instance using the default values.
     */
    Elastic ELASTIC_IN = new ElasticIn();

    /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////
    /**
     * An ElasticOut instance using the default values.
     */
    Elastic ELASTIC_OUT = new ElasticOut();
    /**
     * An ElasticInOut instance using the default values.
     */
    Elastic ELASTIC_IN_OUT = new ElasticInOut();

    /**
     * The basic function for easing.
     *
     * @param t the time (either frames or in seconds/milliseconds)
     * @param b the beginning value
     * @param c the value changed
     * @param d the duration time
     * @return the eased value
     */
    float ease(float t, float b, float c, float d);

    /**
     * A base class for elastic easings.
     */
    abstract class Elastic implements EasingExpend {
        private float amplitude;
        private float period;

        /**
         * Creates a new Elastic easing with the specified settings.
         *
         * @param amplitude the amplitude for the elastic function
         * @param period    the period for the elastic function
         */
        public Elastic(float amplitude, float period) {
            this.amplitude = amplitude;
            this.period = period;
        }

        /**
         * Creates a new Elastic easing with default settings (-1f, 0f).
         */
        public Elastic() {
            this(-1f, 0f);
        }

        /**
         * Returns the period.
         *
         * @return the period for this easing
         */
        public float getPeriod() {
            return period;
        }

        /**
         * Sets the period to the given value.
         *
         * @param period the new period
         */
        public void setPeriod(float period) {
            this.period = period;
        }

        /**
         * Returns the amplitude.
         *
         * @return the amplitude for this easing
         */
        public float getAmplitude() {
            return amplitude;
        }

        /**
         * Sets the amplitude to the given value.
         *
         * @param amplitude the new amplitude
         */
        public void setAmplitude(float amplitude) {
            this.amplitude = amplitude;
        }
    }

    /**
     * An Elastic easing used for ElasticIn functions.
     */
    class ElasticIn extends Elastic {
        public ElasticIn(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticIn() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            return -(a * (float) Math.pow(2, 10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        }
    }

    /**
     * An Elastic easing used for ElasticOut functions.
     */
    class ElasticOut extends Elastic {
        public ElasticOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < Math.abs(c)) {
                //a = c;
                s = p / 4;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            return a * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
        }
    }

    /**
     * An Elastic easing used for ElasticInOut functions.
     */
    class ElasticInOut extends Elastic {
        public ElasticInOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticInOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d / 2) == 2) return b + c;
            if (p == 0) p = d * (.3f * 1.5f);
            float s = 0;
            if (a < Math.abs(c)) {
                a = c;
                s = p / 4f;
            } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            if (t < 1)
                return -.5f * (a * (float) Math.pow(2, 10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
            return a * (float) Math.pow(2, -10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p) * .5f + c + b;
        }
    }

}
