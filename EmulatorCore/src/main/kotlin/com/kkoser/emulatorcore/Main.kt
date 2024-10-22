package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.DefaultInterruptHandler
import com.kkoser.emulatorcore.gpu.*
import com.kkoser.emulatorcore.io.Joypad
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.CartridgeFactory
import com.kkoser.emulatorcore.memory.MemoryBus
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

fun main(vararg args: String) {

    val gameFile = File("/Users/kkoser/Projects/GBK/test.gb")
//    val gameFile = File("/Users/kkoser/Downloads/01-special.gb")
//    val gameFile = File("/Users/kkoser/Downloads/07-jr,jp,call,ret,rst.gb")
//    val gameFile = File("/Users/kkoser/Downloads/03-op sp,hl.gb")
//    val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/cpu_instrs.gb") // infinite increasing numbers?
//    val gameFile = File("/Users/kkoser/Downloads/08-misc instrs.gb")
//    val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/source/test.gb")

    val rom = CartridgeFactory.getCartridgeForFile(gameFile)
    val timer = Timer()
    val lcd = Lcd()
    val gpu = Gpu(lcd, NoOpRenderer)
    val dma = Dma()
    val interruptHandler = DefaultInterruptHandler()
    val joyPad = Joypad(interruptHandler)
    val memoryBus = MemoryBus(rom, timer, interruptHandler, lcd, dma, gpu, joyPad)
    val cpu = Cpu(memoryBus, true)
    val emulator = Emulator(cpu, memoryBus, interruptHandler, timer, lcd, dma)

    try {
        emulator.run()
    } catch (e: Exception) {
        Logger.getGlobal().log(Level.SEVERE, "caught exception")
        Logger.getGlobal().log(Level.SEVERE, e.message, e.stackTrace)
        throw e
    }
}