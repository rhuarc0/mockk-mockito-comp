package com.example.mockkdemo

interface Animal {
    fun say(): String
    fun say(times: Int): String
    fun move(meters: Int): String
    fun sayAndMove(times: Int, meters: Int): String

}

