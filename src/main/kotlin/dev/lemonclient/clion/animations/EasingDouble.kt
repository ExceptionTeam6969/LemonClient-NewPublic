package dev.lemonclient.clion.animations

import dev.lemonclient.clion.animations.AnimationFlag
import net.minecraft.util.math.Vec2f

class EasingDouble(pos: Vec2f) {
    private var lastPos: Vec2f
    private var newPos: Vec2f
    private val offset: Vec2f
        get() = Vec2f(
            (newPos.x - lastPos.x),
            (newPos.y - lastPos.y)
        )
    private val animationX: AnimationFlag
    private val animationY: AnimationFlag
    private var startTime: Long

    init {
        lastPos = pos
        newPos = pos
        animationX = AnimationFlag(Easing.IN_OUT_EXPO, 600f).also {
            it.forceUpdate(pos.x, pos.x)
        }
        animationY = AnimationFlag(Easing.IN_OUT_EXPO, 600f).also {
            it.forceUpdate(pos.y, pos.y)
        }
        startTime = System.currentTimeMillis()
    }

    fun reset() {
        animationX.forceUpdate(0f, 0f)
        animationY.forceUpdate(0f, 0f)
    }

    fun updatePos(pos: Vec2f) {
        lastPos = newPos
        newPos = pos
    }

    fun forceUpdatePos(pos: Vec2f) {
        animationX.forceUpdate(pos.x, pos.x)
        animationY.forceUpdate(pos.x, pos.y)
    }

    fun getUpdate() = Vec2f(
        animationX.getAndUpdate(offset.x + lastPos.x),
        animationY.getAndUpdate(offset.y + lastPos.y)
    )
}
