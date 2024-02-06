package dev.lemonclient.clion.animations.plus

import dev.lemonclient.LemonClient.mc
import dev.lemonclient.clion.animations.AnimationFlag
import dev.lemonclient.clion.animations.Easing
import org.lwjgl.opengl.GL11

class ScaleEasing @JvmOverloads constructor(easing: Easing = Easing.LINEAR, animationTime: Float = 700F) {
    var easing = easing
        set(value) {
            field = value
            animation = AnimationFlag(value, animationTime)
        }
    var animationTime = animationTime
        set(value) {
            field = value
            animation = AnimationFlag(easing, value)
        }
    private var animation: AnimationFlag

    init {
        this.animation = AnimationFlag(easing, animationTime)
        reset()
    }

    fun start() {
        GL11.glPushMatrix()
        val percent = animation.getAndUpdate(1F)
        GL11.glTranslated(mc.window.scaledWidth / 2.0, mc.window.scaledHeight / 2.0, 0.0)
        GL11.glScalef(percent, percent, 0F)
        GL11.glTranslated(-mc.window.scaledWidth / 2.0, -mc.window.scaledHeight / 2.0, 0.0)
    }

    fun end() {
        GL11.glPopMatrix()
    }

    fun reset() {
        animation.forceUpdate(0F, 0F)
    }
}
