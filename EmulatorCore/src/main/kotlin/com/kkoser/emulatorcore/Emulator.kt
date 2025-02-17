package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.memory.MemoryBus
import java.util.logging.Level
import java.util.logging.Logger

const val CLOCK_SPEED = 1/4194304

class Emulator(val cpu: Cpu,
               val memoryBus: MemoryBus,
               val interruptHandler: InterruptHandler,
               val timer: Timer,
               val lcd: Lcd,
               val dma: Dma) {

    fun run() {
        while (true) {
            val startTime = System.currentTimeMillis()

            interruptHandler.handleInterrupts(cpu)
            val cycles = cpu.tick()
            timer.tick(cycles, interruptHandler)
            lcd.tick(cycles, interruptHandler)
            dma.tick(cycles, memoryBus)

            val endTime = System.currentTimeMillis()
            val expectedTime = cycles * CLOCK_SPEED
            val diffTime = endTime - startTime
            if (diffTime < expectedTime) {
                Thread.sleep(expectedTime - diffTime)
            }
        }
    }
}