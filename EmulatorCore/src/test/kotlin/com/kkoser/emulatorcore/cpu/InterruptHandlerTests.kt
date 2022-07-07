package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.TestMemory
import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.io.Joypad
import com.kkoser.emulatorcore.memory.MemoryBus
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.mockito.Mockito

class InterruptHandlerTests {
    val handler: InterruptHandler = DefaultInterruptHandler()
    val lcd = Lcd()
    val timer = Timer()
    val testMemory = TestMemory()
    val dma = Dma()
    val gpu = Gpu(lcd, NoOpRenderer)
    val joyPad = Joypad(handler)
    val memory = MemoryBus(testMemory, timer, handler, lcd, dma, gpu, joyPad)
    val cpu = Cpu(memory, false)

    @Test
    fun respectInterruptOrder() {
        for (interrupt in InterruptHandler.Interrupt.values()) {
            handler.interrupt(interrupt)
        }
    }

    @Test
    fun performInterruptWhenRequestedManuallyAndEnabled() {
        cpu.pc = 0x1234
        handler.registerIE = 0x04
        handler.registerIF = 0x04
        handler.handleInterrupts(cpu)
        assert(cpu.pc == InterruptHandler.Interrupt.CLOCK.location)
        cpu.ret()
        assert(cpu.pc == 0x1234)
        val sp = cpu.registers.get(Registers.Bit16.SP)
        val oldAddr = sp - 2
        assert(cpu.memory.read(oldAddr) == 0x34)
        assert(cpu.memory.read(oldAddr + 1) == 0x12)
    }
}