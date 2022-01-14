package com.example.mockkdemo

class ManWithFeedableAnimal(
    private val animal: FeedableAnimal
) {

    fun callAnimal(call: String): String {
        return call + " " + animal.say()
    }

    fun superCallAnimal(call: String, times: Int): String {
        return call + " " + animal.say(times)
    }

    fun callAndPutAccessory(call: String, on: Boolean) {
        call + " " + animal.say()
        animal.putAccessory(on)
    }

    fun checkAccessory(): Boolean {
        return animal.hasAccessory()
    }

}
