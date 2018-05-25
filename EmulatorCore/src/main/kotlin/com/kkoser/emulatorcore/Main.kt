package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu

fun main(vararg args: String) {
    // Memory initialized to 0s
    val memory = Array<Int>(10000, {0})
    val cpu = Cpu(memory)

    println("Hello from main.kt")
}