package com.example.mockkdemo

import kotlin.math.min

class FeedableDog(startingEnergy: Int = 15) : Dog(), FeedableAnimal {

    private var hasAccessory: Boolean = false
    private var energy: Int = startingEnergy

    private fun spendEnergy(spent: Int): Boolean {
        return if (spent > energy){
            false
        } else {
            energy -= spent
            true
        }
    }

    override fun say(): String {
        return if (spendEnergy(1))
            "I'm feedable dog. Bark."
        else
            "I won't do it"
    }

    override fun move(meters: Int): String {
        return if (spendEnergy(10))
            super.move(meters)
        else
            "I won't do it"
    }

    override fun putAccessory(on: Boolean) {
        hasAccessory = on
    }

    override fun hasAccessory(): Boolean {
        return hasAccessory
    }

    override fun feed(food: Int): Int {
        val consumedFood = min(100 - energy, food)
        energy += consumedFood
        return consumedFood
    }

    override fun hungerStatus(): HungerStatus {
        return HungerStatus.values().first { it.lowerBound <= energy && it.upperBound >= energy }
    }
}
