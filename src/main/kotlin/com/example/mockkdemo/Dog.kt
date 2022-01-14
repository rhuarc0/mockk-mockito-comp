package com.example.mockkdemo

import org.springframework.stereotype.Component
import kotlin.math.min

open class Dog : Animal {

    override fun say(): String {
        return "Bark"
    }

    override fun say(times: Int): String {
        return (1..times).joinToString(separator = "-") { say() }
    }

    override fun move(meters: Int): String {
        return "Walked $meters meters"
    }

    override fun sayAndMove(times: Int, meters: Int): String {
        return say(times) + "|" + move(meters)
    }

}

