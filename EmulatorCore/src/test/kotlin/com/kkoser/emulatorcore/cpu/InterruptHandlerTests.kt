package com.kkoser.emulatorcore.cpu

import org.junit.Test

class InterruptHandlerTests {
    val handler: InterruptHandler = DefaultInterruptHandler()

    @Test
    fun respectInterruptOrder() {
        for (interrupt in InterruptHandler.Interrupt.values()) {
            handler.interrupt(interrupt)
        }
    }
}