package com.example.mockkdemo

enum class HungerStatus(val lowerBound: Int, val upperBound: Int) {
    STARVING(0, 20),
    HUNGRY(21, 50),
    NOT_HUNGRY(51, 80),
    FULL(81, 100)
}
