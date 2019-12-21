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
        var totalInstructions = 0L
        while (cpu.pc <= 0x100){
            interruptHandler.handleInterrupts(cpu)
            val cycles = cpu.tick()
            totalInstructions++
            if (totalInstructions % 1000 == 0L) {
                Logger.getGlobal().log(Level.INFO, "total instructions $totalInstructions")
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
                val second = memoryBus.read(0xFE00 + (x*y))

                System.out.print(first.toHexString() + " ; " + second.toHexString())
            }
            System.out.println()
        }
    }
}