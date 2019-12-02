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
        for (i in 0..2500000){
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
    }
}