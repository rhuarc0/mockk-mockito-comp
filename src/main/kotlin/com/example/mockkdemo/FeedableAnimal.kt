package com.example.mockkdemo

interface FeedableAnimal : Animal {
    fun putAccessory(on: Boolean)
    fun hasAccessory(): Boolean
    fun feed(food: Int): Int
    fun hungerStatus(): HungerStatus
}
