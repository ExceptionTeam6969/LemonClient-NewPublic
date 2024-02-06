/*
 * Copyright (c) 2023 CLion 保留所有权利。 All Right Reserved.
 */

package dev.lemonclient.clion.animations.plus

import dev.lemonclient.clion.animations.AnimationFlag
import dev.lemonclient.clion.animations.Easing
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class EasingSingle @JvmOverloads constructor(pos: Float, length: Float = 400f, easing: Easing = Easing.OUT_EXPO) :
    ReadWriteProperty<Any, Float> {
    private var lastPos: Float
    private var newPos: Float
    private val offset: Float
        get() = (newPos - lastPos)

    private val animationX: AnimationFlag
    private var startTime: Long

    init {
        lastPos = pos
        newPos = pos
        animationX = AnimationFlag(easing, length).also {
            it.forceUpdate(pos, pos)
        }
        startTime = System.currentTimeMillis()
    }

    fun reset() = animationX.forceUpdate(0f, 0f)

    fun updatePos(pos: Float) {
        lastPos = newPos
        newPos = pos
    }

    fun forceUpdatePos(pos: Float) = animationX.forceUpdate(pos, pos)

    fun getUpdate() = animationX.getAndUpdate(offset + lastPos)
    override fun getValue(thisRef: Any, property: KProperty<*>): Float = getUpdate()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) = updatePos(value)
}
