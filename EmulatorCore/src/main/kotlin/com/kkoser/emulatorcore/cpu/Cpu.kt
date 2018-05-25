package com.kkoser.emulatorcore.cpu

class Cpu constructor(memory: Array<Int>) {
    // These values need to be internal so they can be accessed and modified from the extension methods
    internal val registers = Registers()
    internal var pc = 0x100
    internal var stackPointer = 0xFFFE
    internal var memory: Array<Int>

    init {
        this.memory = memory
    }

    fun tick() {
        // Read opcode
        // Perform op
        pc++

        val opcode = 0x0
        val operation = OpCodes.opCodes[opcode] ?: throw IllegalArgumentException("Unsupported operation type: $opcode")
        operation.invoke(this)
    }

}

/**
 * A class to store all the register state of the CPU
 *
 * Note that the cpu registers can actually only hold Byte values (0-255). However, there is no unsiged Byte in
 * Kotlin, so in order to avoid complex conversions, we just use Ints instead with some custom wrapping logic to ensure
 * the behavior matches an unsigned Byte
 */
internal class Registers {
    companion object {
        val MAX_VALUE = 255
        enum class Flag(val bit: Int) {
            Z(7), N(6), H(5), C(4)
        }
    }

    // Starting values for these found here: http://www.codeslinger.co.uk/pages/projects/gameboy/hardware.html
    var a: Int = 0x01
        set(value) {
            field = setValue(value)
        }
    var b: Int = 0xB0
        set(value) {
            field = setValue(value)
        }
    var c: Int = 0x00
        set(value) {
            field = setValue(value)
        }
    var d: Int = 0x13
        set(value) {
            field = setValue(value)
        }
    var e: Int = 0x00
        set(value) {
            field = setValue(value)
        }
    var f: Int = 0xD8
        set(value) {
            field = setValue(value)
        }
    var h: Int = 0x01
        set(value) {
            field = setValue(value)
        }
    var l: Int = 0x4D
        set(value) {
            field = setValue(value)
        }

    // Computed WORD registers
    var af: Int = 0
        get() {
            return a.toIntWithLowerInt(f)
        }
        set(value) {
            field = setValue(value)
        }
    var bc: Int = 0
        get() {
            return b.toIntWithLowerInt(c)
        }
        set(value) {
            field = setValue(value)
        }
    var de: Int = 0
        get() {
            return d.toIntWithLowerInt(e)
        }
        set(value) {
            field = setValue(value)
        }
    var hl: Int = 0
        get() {
            return h.toIntWithLowerInt(l)
        }
        set(value) {
            field = setValue(value)
        }

    fun setValue(value: Int): Int {
        // If we overwrote, set the carry flag
        // TODO: We maybe shouldn't be setting this in all cases?
        if (value > MAX_VALUE) {
            f = Flag.C.bit
        }
        return value % MAX_VALUE
    }
}

fun Int.toIntWithLowerInt(lowerInt: Int): Int {
    return ((this shl 8) or lowerInt)
}