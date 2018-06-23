package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.memory.MemoryBus

class Emulator(val cpu: Cpu,
               val memoryBus: MemoryBus,
               val interruptHandler: InterruptHandler,
               val timer: Timer) {

    fun run() {
        for (i in 1..70224){
            val cycles = cpu.tick()
            timer.tick(cycles, interruptHandler)
            interruptHandler.handleInterrupts(cpu)
        }
    }
}