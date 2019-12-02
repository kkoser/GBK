package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.InterruptHandler

class TestInterruptHandler  : InterruptHandler {
    override var registerIE = 0b11111
    override var registerIF = 0b11111
    var interrupts: MutableList<InterruptHandler.Interrupt> = mutableListOf()

    fun setup() {
        interrupts.clear()
    }
    override fun interrupt(interrupt: InterruptHandler.Interrupt) {
        interrupts.add(interrupt)
    }


    override fun handleInterrupts(cpu: Cpu) {
        // no-op
    }

    override fun toggleInterrupt(interrupt: InterruptHandler.Interrupt) {
        // no-op
    }
}