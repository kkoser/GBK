package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.DefaultInterruptHandler
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.MemoryBus
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

fun main(vararg args: String) {

    val gameFile = File("/home/kyleskoser/GBK/tetris.gb")

    val rom = BasicROM(gameFile)
    val timer = Timer()
    val interruptHandler = DefaultInterruptHandler()
    val memoryBus = MemoryBus(rom, timer, interruptHandler)
    val cpu = Cpu(memoryBus)
    val emulator = Emulator(cpu, memoryBus, interruptHandler, timer)

    try {
        emulator.run()
    } catch (e: Exception) {
        Logger.getGlobal().log(Level.SEVERE, e.message, e.stackTrace)
        throw e
    }
}