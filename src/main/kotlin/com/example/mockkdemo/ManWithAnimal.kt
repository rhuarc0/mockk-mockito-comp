package com.example.mockkdemo

class ManWithAnimal(
    private val animal: Animal
) {

    fun callAnimal(call: String): String {
        return call + " " + animal.say()
    }

    fun superCallAnimal(call: String, times: Int): String {
        return call + " " + animal.say(times)
    }
}
