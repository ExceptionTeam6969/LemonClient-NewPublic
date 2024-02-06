/*
 * Copyright (c) 2023 CLion 保留所有权利。 All Right Reserved.
 */

package dev.lemonclient.clion.animations

fun interface InterpolateFunction {
    operator fun invoke(time: Long, prev: Float, current: Float): Float
}
