package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.memory.MemoryBus
import java.util.logging.Level
import java.util.logging.Logger

class Emulator(val cpu: Cpu,
               val memoryBus: MemoryBus,
               val interruptHandler: InterruptHandler,
               val timer: Timer,
               val lcd: Lcd) {

    fun run() {
//        Thread.sleep(15000)
        var totalInstructions = 0L
        while (true){
            interruptHandler.handleInterrupts(cpu)
            val cycles = cpu.tick()
            totalInstructions++
            if (totalInstructions % 1000 == 0L) {
//                Logger.getGlobal().log(Level.INFO, "total instructions $totalInstructions")
            }
//            System.out.println("total cycles $totalCycles")
            timer.tick(cycles, interruptHandler)
            lcd.tick(cycles, interruptHandler)
        }

        // dump vram
        // print the first tile and quit
        // print out the first tile
        for (x in 0..7) {
            for (y in 0..7) {
                val first = memoryBus.read(0xFE00 + (x*y))
                val second = memoryBus.read((0xFE00 + (x*y)) + 1)

                System.out.print(first.toHexString() + " ; " + second.toHexString())
            }
            System.out.println()
        }
        println()
        println()

        for (x in 0..Math.abs(0x8000-0x9FFF)) {
            val pos = x + 0x8000
            print(memoryBus.read(pos).toHexString())
            if (x % 16 == 0 && x != 0) println()
        }

        println()
        println("LCD Control: ${Integer.toBinaryString(lcd.control)}")

        val bgMap = getBackgroundMap()
        for (line in bgMap) {
            for (tile in line) {
                print(" ${tile.toHexString()} ")

            }
            println()
        }
    }

    private fun getBackgroundMap(): Array<Array<Int>> {
        // Subtract the start of VRAM since we aren't going through the memory for this
        val BG_START = 0x9800
        val BG_LENGTH = 32

        val ret = Array<Array<Int>>(32) {Array(32) {0}}

        for (i in 0 until BG_LENGTH) {
            val arr = ret[i]
            for (j in 0 until BG_LENGTH) {
                arr[j] = memoryBus.read(BG_START + (32*i) + j)
            }
        }

        return ret
    }
}