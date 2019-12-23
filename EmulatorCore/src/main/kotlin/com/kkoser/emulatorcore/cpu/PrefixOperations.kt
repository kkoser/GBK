package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toUnsigned8BitInt

fun Cpu.swapImmediate(register: Registers.Bit8) {
    val value = swapValue(registers.get(register))
    registers.set(register, value)
    setFlag(Cpu.Flag.Z, value == 0)
}

fun Cpu.swapIndirect(register: Registers.Bit16) {
    val location = registers.get(register)
    memory.write(location, swapValue(memory.read(location)))
}

private fun swapValue(value: Int): Int {
    val base = value and 0xff
    val lower = value and 0xf
    val higher = base and 0xf0

    return (lower shl 4) and (higher shr 4)
}

fun Cpu.checkBit(register: Registers.Bit8, bit: Int) {
    setFlag(Cpu.Flag.Z, !registers.get(register).checkBit(bit))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, true)
}

// region Rotates/Shifts
/**
 * Rotate left into the carry, but do not go through. So the carry flag will be the value of the highest bit of the old
 * value, but the lowest bit of the new value will always be 0
 */
fun Cpu.rlc(location: Registers.Bit8) {
    val arg = registers.get(location)
    var result = (arg shl 1).toUnsigned8BitInt()
    if (arg and (1 shl 7) != 0) {
        result = result or 1
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

/**
 * Rotate left THROUGH the carry bit. That is, the top bit of the old value is set as the carry, and the carry bit
 * becomes the first bit of the new value
 */
fun Cpu.rl(location: Registers.Bit8) {
    val arg = registers.get(location).toUnsigned8BitInt()
    val bottomBit = if (checkFlag(Cpu.Flag.C)) 0x01 else 0x00
    val result = (arg shl 1).toUnsigned8BitInt() or bottomBit
    setFlag(Cpu.Flag.C, arg.checkBit(7))
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rrc(location: Registers.Bit8) {
    val arg = registers.get(location)
    var result = (arg ushr 1).toUnsigned8BitInt()
    if (arg and 1 != 0) {
//        result = result or 1
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rr(location: Registers.Bit8) {
    val arg = registers.get(location)
    var result = (arg ushr 1).toUnsigned8BitInt()

    // Shift the carry flag into the top bit
    val carryMask = (if (checkFlag(Cpu.Flag.C)) 1 else 0) shl 7
    result = result or carryMask

    // Shift the bottom bit into the carry flag
    if (arg and 1 != 0) {
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

// endregion