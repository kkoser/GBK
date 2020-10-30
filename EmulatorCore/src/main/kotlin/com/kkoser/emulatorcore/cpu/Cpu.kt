package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.getHigh8Bits
import com.kkoser.emulatorcore.getLow8Bits
import com.kkoser.emulatorcore.memory.MemoryBus
import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toIntWithLowerInt
import com.kkoser.emulatorcore.toUnsigned16BitInt
import com.kkoser.emulatorcore.toUnsigned8BitInt
import jdk.internal.org.objectweb.asm.Opcodes
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode
import java.util.logging.Level
import java.util.logging.Logger

class Cpu constructor(memory: MemoryBus, debug: Boolean = true) {
    // These values need to be internal so they can be accessed and modified from the extension methods
    internal val registers = Registers()
    internal var pc = 0x00
//    internal var pc = 0x100
    internal val memory = memory
    internal var halted = false
    internal var ime = true

    private var ticks = 0
    private var cycleCount = 0
    private var debug: Boolean = debug

    enum class Flag(val mask: Int) {
        Z(0b10000000), N(0b01000000), H(0b00100000), C(0b00010000)
    }

    /**
     * Performs the next pending operation at the current pc
     * @return The number of cycles this operation took, so that other pieces of hardware can be emulated the same
     * number of cycles
     */
    fun tick(): Int {
        if (halted) {
            Thread.sleep(1000)
            return 0
        }
        ticks++

        if (pc > 0xFFFF) {
            throw RuntimeException("pc is out of memory memory range at ${pc.toHexString()}")
        }

        var usePrefixOpcodes = false
        val oldPc = pc
        var opcode = memory.read(pc)
        if (opcode == 0xCB) {
            // TODO: Are there any prefixes that read values past current PC? Don't think so
            // If there are, this may break things
            opcode = memory.read(pc + 1)
            usePrefixOpcodes = true
        }
        val operation = if (usePrefixOpcodes) PrefixOpcodes.opCodes[opcode] else OpCodes.opCodes[opcode]

        val cyclesTaken = performOperation(operation, oldPc)
        cycleCount += cyclesTaken

        return cyclesTaken
    }

    private fun performOperation(operation: Operation?, oldPc: Int): Int {
        if (operation == null) {
            throw IllegalArgumentException("Unsupported operation type: " + Integer.toHexString(memory.read(oldPc)))
        }
        if (ticks % 1 == 0) {
//            System.out.println("total ticks $ticks, cycles:$cycleCount")
            if (pc >= 0x100) {
                printDebugState(operation)
            }
        }

        operation.operation(this)

//        if (pc == 0x282a) {
//            throw RuntimeException("filling vram?")
//        }
        if (!operation.isJump) {
            pc += operation.numBytes
            return operation.cycles
        } else {
            // PC has already been moved, so we don't need to move it
            if (oldPc == pc) {
                // Jump was NOT taken, so move the pc ourselves
                pc += operation.numBytes
                return operation.notTakenCycles
            } else {
                return operation.cycles
            }
        }
    }

    internal fun setFlag(flag: Flag, on: Boolean = true) {
        if (on) {
            registers.set(Registers.Bit8.F, registers.get(Registers.Bit8.F) or flag.mask)
        } else {
            registers.set(Registers.Bit8.F, registers.get(Registers.Bit8.F) and flag.mask.inv())
        }
    }

    /**
     * Returns whether or not a flag is currently set to true
     */
    fun checkFlag(flag: Flag): Boolean {
        return registers.get(Registers.Bit8.F) and flag.mask > 0
    }

    fun printDebugState(operation: Operation) {
//        if (!debug)
//            return
//        System.out.println("AF=${registers.get(Registers.Bit16.AF).toHexString()}, BC=${registers.get(Registers.Bit16.BC).toHexString()}, DE=${registers.get(Registers.Bit16.DE).toHexString()}, HL=${registers.get(Registers.Bit16.HL).toHexString()}, SP=${registers.get(Registers.Bit16.SP).toHexString()}, PC=${pc.toHexString()}, ${describeFlags()}----, ${operation.title}")
        val af = registers.get(Registers.Bit16.AF)
        val bc = registers.get(Registers.Bit16.BC)
        val de = registers.get(Registers.Bit16.DE)
        val hl = registers.get(Registers.Bit16.HL)
        val sp = registers.get(Registers.Bit16.SP)

        fun getFlags(): String  {
                val result = StringBuilder()
                result.append(if (checkFlag(Flag.Z)) 'Z' else '-')
                result.append(if (checkFlag(Flag.N)) 'N' else '-')
                result.append(if (checkFlag(Flag.H)) 'H' else '-')
                result.append(if (checkFlag(Flag.C)) 'C' else '-')
                result.append("----")
                return result.toString()
        }

        val flags = getFlags()



        println(String.format("AF=%04x, BC=%04x, DE=%04x, HL=%04x, SP=%04x, PC=%04x, %s, %s", af, bc, de, hl, sp, pc, flags.toString(), operation.title))

    }

    fun checkStack(): Int {
        val x = popStack()
        push(x)

        return x
    }

    fun describeFlags(): String {
        val z = if (checkFlag(Flag.Z)) "Z" else "-"
        val n = if (checkFlag(Flag.N)) "N" else "-"
        val h = if (checkFlag(Flag.H)) "H" else "-"
        val c = if (checkFlag(Flag.C)) "C" else "-"

        return z+n+h+c
    }
}

/**
 * A class to store all the register state of the CPU
 *
 * Note that the cpu registers can actually only hold Byte values (0-255). However, there is no unsiged Byte in
 * Kotlin, so in order to avoid complex conversions, we just use Ints instead with some custom wrapping logic to ensure
 * the behavior matches an unsigned Byte
 */
class Registers {
    companion object {
        const val MAX_VALUE_8 = 0xFF
        const val MAX_VALUE_16 = 0xFFFF
        const val SP_START = 0xFFFE
    }

    // Initially, since there are only 8 registers, each was a separate property. However, since many instructions
    // perform the same operation on with different registers, this approach allows us to write less duplicated opcodes
    private val registers = Array(Bit8.values().size, {0})
    private var stackPointer = SP_START

    enum class Bit8(val index: Int) {
        A(0), B(1), C(2), D(3), E(4), F(5), H(6), L(7), UNUSED(-1)
    }

    enum class Bit16(val high: Bit8, val low: Bit8) {
        AF(Bit8.A, Bit8.F), BC(Bit8.B, Bit8.C), DE(Bit8.D, Bit8.E), HL(Bit8.H, Bit8.L), SP(Bit8.UNUSED, Bit8.UNUSED)
    }

//    init {
//        set(Bit16.AF, 0x01B0)
//        set(Bit16.BC, 0x0013)
//        set(Bit16.DE, 0x00D8)
//        set(Bit16.HL, 0x014D)
//    }
    init {
        set(Bit16.AF, 0)
        set(Bit16.BC, 0)
        set(Bit16.DE, 0)
        set(Bit16.HL, 0)
    }


    // Starting values for these found here: http://www.codeslinger.co.uk/pages/projects/gameboy/hardware.html

    fun get(register: Bit8) : Int {
        return registers[register.index]
    }

    fun get(register: Bit16): Int {
        if (register == Bit16.SP) {
            return stackPointer
        }

        val highVale = get(register.high)
        val lowValue = get(register.low)

        return highVale.toIntWithLowerInt(lowValue)
    }

    fun set(register: Bit8, value: Int) {
        if (register == Bit8.UNUSED) {
            return
        }

        if (register == Bit8.F) {
            // We need special handling here, since the lower 4 bits are _always_ 0 for the flag register
            // Based on http://www.devrs.com/gb/files/opcodes.html
            registers[register.index] = value and 0xF0
        } else {
            registers[register.index] = value.toUnsigned8BitInt()
        }
    }

    fun set(register: Bit16, value: Int) {
        if (register == Bit16.SP) {
            // restrict the value to 16 bits
            stackPointer = value.toUnsigned16BitInt()
            return
        }

        set(register.high, value.getHigh8Bits())
        set(register.low, value.getLow8Bits())
    }
}

