package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.memory.MemoryBus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CpuTests {
    val timer = Timer()
    val testMemory = TestMemory()
    val interruptHandler = DefaultInterruptHandler()
    val lcd = Lcd()
    val dma = Dma()
    val gpu = Gpu(lcd, NoOpRenderer())
    val memory = MemoryBus(testMemory, timer, interruptHandler, lcd, dma, gpu)
    val cpu = Cpu(memory, false)

    @Before
    fun reset() {
        // Usually, the cpu starts at 0x100, but for tests we don't want the splash screen so we force it to 0
        cpu.pc = 0

        // Clear all registers (which will also clear all flags)
        for (register in Registers.Bit8.values()) {
            cpu.registers.set(register, 0)
        }
        for (register in Registers.Bit16.values()) {
            cpu.registers.set(register, 0)
        }

        cpu.registers.set(Registers.Bit16.SP, Registers.SP_START)
    }

    @Test
    fun loadImmediate8() {
        runCpuWithInstructions(arrayOf(
                0x06, 0xCC
        ))

        assertEquals(cpu.registers.get(Registers.Bit8.B), 0xCC)
    }

    @Test
    fun loadImmediate16() {
        runCpuWithInstructions(arrayOf(
                0x01, 0xCC, 0xBB
        ))

        assertEquals(cpu.registers.get(Registers.Bit16.BC), 0xBBCC)
        assertEquals(0xBB, cpu.registers.get(Registers.Bit8.B))
        assertEquals(0xCC, cpu.registers.get(Registers.Bit8.C))
    }

    @Test
    fun loadDirect16() {
        cpu.registers.set(Registers.Bit8.A, 0xFC)
        cpu.registers.set(Registers.Bit16.BC, 0xC300)
        runCpuWithInstructions(arrayOf(
                0x02
        ))

        assertEquals(0xFC, cpu.memory.read(0xC300))
    }

    @Test
    fun loadImmediateAndIncrement8() {
        runCpuWithInstructions(arrayOf(
                0x06, 0xCC, // LD B
                0x04 // inc B

        ))

        assertEquals(0xCD, cpu.registers.get(Registers.Bit8.B))
    }

    @Test
    fun loadImmediateAndIncrement16() {
        runCpuWithInstructions(arrayOf(
                0x01, 0xCC, 0xBB, // LD BC
                0x03 // inc BC

        ))

        assertEquals(0xBBCD, cpu.registers.get(Registers.Bit16.BC))
    }

    @Test
    fun pushAndPopSameValue() {
        cpu.push(0x5060)
        cpu.popInto(Registers.Bit16.HL)

        assertEquals(0x5060, cpu.registers.get(Registers.Bit16.HL))
    }

    @Test
    fun pushAndPopMultipleValues() {
        cpu.push(0x5060)
        cpu.push(0x1234)
        cpu.push(0x5678)
        cpu.popInto(Registers.Bit16.HL)
        cpu.popInto(Registers.Bit16.DE)
        cpu.popInto(Registers.Bit16.BC)

        assertEquals(0x5678, cpu.registers.get(Registers.Bit16.HL))
        assertEquals(0x1234, cpu.registers.get(Registers.Bit16.DE))
        assertEquals(0x5060, cpu.registers.get(Registers.Bit16.BC))
    }

    private fun runCpuWithInstructions(instructions: Array<Int>) {
        testMemory.setROM(instructions)
        while(cpu.pc < instructions.size) {
            cpu.tick()
        }
    }
}