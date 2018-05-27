package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.MemoryBus
import java.io.File


fun main(vararg args: String) {

    val gameFile = File("/Users/kkoser/Projects/GBK/cpu_tests.gb")

    val rom = BasicROM(gameFile)
    val memoryBus = MemoryBus(rom)
    val cpu = Cpu(memoryBus)

    val firstByte = rom.read(0)
    val secondByte = rom.read(1)
    println("Got byte: ${Integer.toHexString(firstByte)}")
    println("Got second byte: ${Integer.toHexString(secondByte)}")

    for (i in 1..100) {
        cpu.tick()
    }

}