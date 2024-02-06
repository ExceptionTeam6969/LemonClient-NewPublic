package dev.lemonclient

import top.fl0wowp4rty.phantomshield.annotations.Native

@Native
object TimeBomber {
    @JvmStatic
    inline fun shouldBomb(): Boolean = System.currentTimeMillis() >= "HelloWorld".hashCode() * 3888.953634048703
}
