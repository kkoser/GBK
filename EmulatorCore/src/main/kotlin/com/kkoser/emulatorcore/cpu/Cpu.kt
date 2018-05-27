package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.getHigh8Bits
import com.kkoser.emulatorcore.getLow8Bits
import com.kkoser.emulatorcore.memory.MemoryBus
import com.kkoser.emulatorcore.toIntWithLowerInt
import java.util.logging.Level
import java.util.logging.Logger

class Cpu constructor(memory: MemoryBus) {
    // These values need to be internal so they can be accessed and modified from the extension methods
    internal val registers = Registers()
    internal var pc = 0x100
    internal var stackPointer = 0xFFFE
    internal val memory = memory

    enum class Flag(val mask: Int) {
        Z(0b10000000), N(0b001000000), H(0b00100000), C(0b00010000)
    }

    /**
     * Performs the next pending operation at the current pc
     * @return The number of cycles this operation took, so that other pieces of hardware can be emulated the same
     * number of cycles
     */
    fun tick(): Int {
        val opcode = memory.read(pc)
        val operation = OpCodes.opCodes[opcode] ?: throw IllegalArgumentException("Unsupported operation type: $opcode")
        Logger.getGlobal().log(Level.INFO, "Running command ${operation.title} at pc $pc")
        operation.operation(this)

        pc += operation.numBytes

        return operation.cycles
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
        const val MAX_VALUE = 255
    }

    // Initially, since there are only 8 registers, each was a separate property. However, since many instructions
    // perform the same operation on with different registers, this approach allows us to write less duplicated opcodes
    private val registers = Array(Bit8.values().size, {0})

    enum class Bit8(val index: Int) {
        A(0), B(1), C(2), D(3), E(4), F(5), H(6), L(7)
    }

    enum class Bit16(val high: Bit8, val low: Bit8) {
        AF(Bit8.A, Bit8.F), BC(Bit8.B, Bit8.C), DE(Bit8.D, Bit8.E), HL(Bit8.H, Bit8.L)
    }


    // Starting values for these found here: http://www.codeslinger.co.uk/pages/projects/gameboy/hardware.html

    fun get(register: Bit8) : Int {
        return registers[register.index]
    }

    fun get(register: Bit16): Int {
        val highVale = get(register.high)
        val lowValue = get(register.low)

        return highVale.toIntWithLowerInt(lowValue)
    }

    fun set(register: Bit8, value: Int) {
        // If we overwrote, set the carry flag
        // TODO: We maybe shouldn't be setting this in all cases?
        if (value > MAX_VALUE) {
//            registers[Bit8.F.index] = Cpu.Flag.C.bit
        }
        val newValue = value % MAX_VALUE
        if (newValue < 0 || newValue > MAX_VALUE) {
            throw RuntimeException("the value in the 8 bit register is not a valid unsiged byte value, something isnt working right")
        }

        registers[register.index] = newValue
    }

    fun set(register: Bit16, value: Int) {
        set(register.high, value.getHigh8Bits())
        set(register.low, value.getLow8Bits())
    }
}

