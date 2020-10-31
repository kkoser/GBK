package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.TestMemory
import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.add8BitSigned
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.memory.MemoryBus
import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toUnsigned8BitInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        // disable the boot rom
        memory.write(0xFF50, 1)

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

        runCpuWithInstructions(arrayOf(
                0x06, 0xFF, // LD B
                0x04 // inc B

        ))

        assertEquals(0x00, cpu.registers.get(Registers.Bit8.B))
        assertTrue(cpu.checkFlag(Cpu.Flag.Z))
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

    @Test
    fun pushAndPopDifferentRegisters() {
        cpu.registers.set(Registers.Bit16.DE, 0x100)
        cpu.pushRegister(Registers.Bit16.DE)

        cpu.popInto(Registers.Bit16.AF)
        assertEquals(0x100, cpu.registers.get(Registers.Bit16.AF))

        cpu.registers.set(Registers.Bit16.DE, 0x90)
        cpu.pushRegister(Registers.Bit16.DE)

        cpu.popInto(Registers.Bit16.AF)
        assertEquals(0x90, cpu.registers.get(Registers.Bit16.AF))
    }

    @Test
    fun testCustomStackValue()  {
        cpu.push(0x0099)
        val temp = cpu.registers.get(Registers.Bit16.SP)
        cpu.registers.set(Registers.Bit16.SP, 0xCFFF)
        cpu.push(temp)
        cpu.push(0x0555)

        assertEquals(0x0555, cpu.popStack())
        cpu.popInto(Registers.Bit16.SP)
        assertEquals(0x0099, cpu.popStack())
    }

    @Test
    fun incrementSpSigned() {
        cpu.registers.set(Registers.Bit16.SP, 0xCFFF)
        runCpuWithInstructions(arrayOf(
                0xE8,
                0xFF
        ))

        assertEquals(cpu.registers.get(Registers.Bit16.SP), 0xCFFE)
    }

    @Test
    fun loadImmediate8Loop() {
        val values = arrayOf(0x00,0x01,0x0F,0x10,0x1F,0x7F,0x80,0xF0,0xFF)

        val regs = enumValues<Registers.Bit8>().filter { it != Registers.Bit8.F && it != Registers.Bit8.UNUSED }
        for (reg in regs) {
            for (value in values) {
                cpu.memory.write(cpu.pc + 1, value)
                cpu.loadImmediate8(reg)

                println(reg)
                assertEquals("$reg", cpu.registers.get(reg), value)
            }
        }
    }

    @Test
    fun decrementWrap() {
        cpu.registers.set(Registers.Bit8.B, 0)

        cpu.decrement8(Registers.Bit8.B)

        assertEquals(cpu.checkFlag(Cpu.Flag.N), true)
        assertEquals(cpu.checkFlag(Cpu.Flag.H), true)
        assertEquals(cpu.checkFlag(Cpu.Flag.Z), false)
        assertEquals(cpu.registers.get(Registers.Bit8.B), 255)
    }

    @Test
    fun jrNegativeTest() {
        val x = (0xc7c0).add8BitSigned(0xFE)
        assertEquals(x, 0xc7be)

        val y = (0xc7c2).add8BitSigned(0xFE)
        assertEquals(y, 0xc7c0)
    }

    private fun runCpuWithInstructions(instructions: Array<Int>) {
        cpu.pc = 0
        testMemory.setROM(instructions)
        while(cpu.pc < instructions.size) {
            cpu.tick()
        }
    }
}