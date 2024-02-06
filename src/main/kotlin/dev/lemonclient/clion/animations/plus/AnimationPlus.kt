package dev.lemonclient.clion.animations.plus

class AnimationPlus(private val easing: EasingExpend.Elastic, private val length: Float) {

    init {
        easing.amplitude = 10f
        easing.period = 0f
    }

    var prev = 0.0f
    var current = 0.0f
    private var time = System.currentTimeMillis()

    fun forceUpdate(prev: Float, current: Float) {
        this.prev = prev
        this.current = current
        time = System.currentTimeMillis()
    }

    fun getAndUpdate(input: Float): Float {
        val render = easing.ease((System.currentTimeMillis() - time).toFloat(), prev, current - prev, length)
        if (input != current) {
            prev = render
            current = input
            time = System.currentTimeMillis()
        }

        return render
    }

    fun get(input: Float, update: Boolean): Float {
        val render = easing.ease((System.currentTimeMillis() - time).toFloat(), prev, current - prev, length)

        if (update && input != current) {
            prev = render
            current = input
            time = System.currentTimeMillis()
        }

        return render
    }
}
