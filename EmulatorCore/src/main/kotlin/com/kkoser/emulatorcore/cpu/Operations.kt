package com.kkoser.emulatorcore.cpu

// A file to contain extensions for all the different Operations a cpu can take
// Using extensions allows these to be organzied into logical chunks and avoid becoming one massive file
fun Cpu.noop(): Int {
    return 4
}

fun Cpu.add(): Int {
    println("Adding")
    return registers.a + 1;
}