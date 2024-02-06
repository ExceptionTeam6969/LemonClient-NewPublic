/*
 * Copyright (c) 2023 CLion 保留所有权利。 All Right Reserved.
 */

package dev.lemonclient.clion.animations

import dev.lemonclient.clion.animations.Easing
import dev.lemonclient.clion.animations.InterpolateFunction

class AnimationFlag(private val interpolation: InterpolateFunction) {

    constructor(easing: Easing, length: Float) : this({ time, prev, current ->
        easing.incOrDec(Easing.toDelta(time, length), prev, current)
    })

    var prev = 0.0f
    var current = 0.0f
    private var time = System.currentTimeMillis()

    fun forceUpdate(prev: Float, current: Float) {
        this.prev = prev
        this.current = current
        time = System.currentTimeMillis()
    }

    fun getAndUpdate(input: Float): Float {
        val render = interpolation.invoke(time, prev, current)

        if (input != current) {
            prev = render
            current = input
            time = System.currentTimeMillis()
        }

        return render
    }

    fun get(input: Float, update: Boolean): Float {
        val render = interpolation.invoke(time, prev, current)

        if (update && input != current) {
            prev = render
            current = input
            time = System.currentTimeMillis()
        }

        return render
    }
}
