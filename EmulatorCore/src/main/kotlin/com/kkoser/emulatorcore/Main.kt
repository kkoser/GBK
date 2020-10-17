package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.DefaultInterruptHandler
import com.kkoser.emulatorcore.gpu.*
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.MemoryBus
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

fun main(vararg args: String) {

//    val gameFile = File("/Users/kkoser/Projects/GBK/test.gb")
//    val gameFile = File("/Users/kkoser/Downloads/01-special.gb")
    val gameFile = File("/Users/kkoser/Downloads/07-jr,jp,call,ret,rst.gb")
//    val gameFile = File("/Users/kkoser/Downloads/03-op sp,hl.gb")
//    val gameFile = File("/Users/kkoser/Downloads/08-misc instrs.gb")

    val rom = BasicROM(gameFile.inputStream())
    val timer = Timer()
    val lcd = Lcd()
    val gpu = Gpu(lcd, NoOpRenderer())
    val dma = Dma()
    val interruptHandler = DefaultInterruptHandler()
    val memoryBus = MemoryBus(rom, timer, interruptHandler, lcd, dma, gpu)
    val cpu = Cpu(memoryBus, true)
    val emulator = Emulator(cpu, memoryBus, interruptHandler, timer, lcd)

    try {
        emulator.run()
    } catch (e: Exception) {
        Logger.getGlobal().log(Level.SEVERE, "caught exception")
        Logger.getGlobal().log(Level.SEVERE, e.message, e.stackTrace)
        throw e
    }
}