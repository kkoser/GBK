package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.TestInterruptHandler
import com.kkoser.emulatorcore.TestMemory
import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.memory.MemoryBus
import org.junit.Test
import org.junit.Assert.assertEquals

class TimerTests {
    val timer = Timer()
    val testMemory = TestMemory()
    val interruptHandler = TestInterruptHandler()
    val lcd = Lcd()
    val dma = Dma()
    val gpu = Gpu(lcd, NoOpRenderer())
    val memory = MemoryBus(testMemory, timer, interruptHandler, lcd, dma, gpu)

    @Test
    fun overflowDividerAfter256Cycles() {
        for (i in 0..256) {
            assertEquals(timer.dividerCount, 0)
            timer.tick(1, interruptHandler)
        }

        assertEquals(timer.dividerCount, 1)
        assertEquals(interruptHandler.interrupts.size, 0)
    }

    @Test
    fun interruptAt4096Hz() {
        for (i in 0..(1024 * 10)) {
            timer.tick(1, interruptHandler)
        }

        assertEquals(interruptHandler.interrupts.size, 10)
        for (interrupt in interruptHandler.interrupts) {
            assertEquals(interrupt, InterruptHandler.Interrupt.CLOCK)
        }
    }

    @Test
    fun changeClockFrequencyViaMemory() {
        memory.write(0xFF07, 0b111)
        assertEquals(timer.enabled, true)

        for (i in 0..(256*10)) {
            timer.tick(1, interruptHandler)
        }

        assertEquals(interruptHandler.interrupts.size, 10)
        for (interrupt in interruptHandler.interrupts) {
            assertEquals(interrupt, InterruptHandler.Interrupt.CLOCK)
        }
    }

    @Test
    fun disableAndStillRunDivider() {
        memory.write(0xFF07, 0b000)

        assertEquals(timer.enabled, false)

        for (i in 0..(256*10)) {
            timer.tick(1, interruptHandler)
        }

        assertEquals(interruptHandler.interrupts.size, 0)
        assertEquals(timer.dividerCount, 10)
    }

    @Test
    fun resetDividerOnWrite() {
        for (i in 0..400*256) {
            timer.tick(1, interruptHandler)
        }

        assertEquals(timer.dividerCount, 145)
        memory.write(0xFF04, 12345)

        assertEquals(timer.dividerCount, 0)
    }
}