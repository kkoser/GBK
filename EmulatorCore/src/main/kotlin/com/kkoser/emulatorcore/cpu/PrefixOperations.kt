package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.getBit
import com.kkoser.emulatorcore.getHighNibble
import com.kkoser.emulatorcore.getLowNibble
import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toIntWithHighNibble
import com.kkoser.emulatorcore.toIntWithLowerInt
import com.kkoser.emulatorcore.toUnsigned16BitInt
import com.kkoser.emulatorcore.toUnsigned8BitInt

fun Cpu.checkBit(register: Registers.Bit8, bit: Int) {
    setFlag(Cpu.Flag.Z, !registers.get(register).checkBit(bit))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, true)
}

fun Cpu.checkBitIndirect(register: Registers.Bit16, bit: Int) {
    setFlag(Cpu.Flag.Z, memory.read(registers.get(register)).checkBit(bit))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, true)
}

fun Cpu.resetBit(register: Registers.Bit8, bit: Int) {
    val mask = (0 shl bit).inv()
    val arg = registers.get(register)
    registers.set(register, arg and mask)
}

fun Cpu.resetBitIndirect(register: Registers.Bit16, bit: Int) {
    val mask = (0 shl bit).inv()
    val memoryLocation = registers.get(register)
    val arg = memory.read(memoryLocation)
    memory.write(memoryLocation, arg and mask)
}

fun Cpu.setBit(register: Registers.Bit8, bit: Int) {
    val mask = (1 shl bit)
    val arg = registers.get(register)
    registers.set(register, arg or mask)
}

fun Cpu.setBitIndirect(register: Registers.Bit16, bit: Int) {
    val mask = (1 shl bit)
    val memoryLocation = registers.get(register)
    val arg = memory.read(memoryLocation)
    memory.write(memoryLocation, arg or mask)
}

// region Rotates/Shifts
/**
 * Rotate left into the carry, but do not go through. So the carry flag will be the value of the highest bit of the old
 * value, but the lowest bit of the new value will always be 0
 */
fun Cpu.rlc(location: Registers.Bit8, forceZToOff: Boolean = false) {
    val arg = registers.get(location)
    var result = (arg shl 1).toUnsigned8BitInt()
    if (arg and (1 shl 7) != 0) {
        result = result or 1
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }

    if (forceZToOff) {
        setFlag(Cpu.Flag.Z, false)
    } else {
        setFlag(Cpu.Flag.Z, result == 0)
    }
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rlcIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)
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

    memory.write(memoryLocation, result)
}

/**
 * Rotate left THROUGH the carry bit. That is, the top bit of the old value is set as the carry, and the carry bit
 * becomes the first bit of the new value
 */
fun Cpu.rl(location: Registers.Bit8, forceZToOff: Boolean = false) {
    val arg = registers.get(location).toUnsigned8BitInt()
    val bottomBit = if (checkFlag(Cpu.Flag.C)) 0x01 else 0x00
    val result = (arg shl 1).toUnsigned8BitInt() or bottomBit
    setFlag(Cpu.Flag.C, arg.checkBit(7))

    if (forceZToOff) {
        setFlag(Cpu.Flag.Z, false)
    } else {
        setFlag(Cpu.Flag.Z, result == 0)
    }
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rlIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation).toUnsigned8BitInt()
    val bottomBit = if (checkFlag(Cpu.Flag.C)) 0x01 else 0x00
    val result = (arg shl 1).toUnsigned8BitInt() or bottomBit
    setFlag(Cpu.Flag.C, arg.checkBit(7))
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rrc(location: Registers.Bit8, forceZToOff: Boolean = false) {
    val arg = registers.get(location)
    var result = (arg ushr 1).toUnsigned8BitInt()
    if (arg and 1 != 0) {
        result = result or (1 shl 7)
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }
    if (forceZToOff) {
        setFlag(Cpu.Flag.Z, false)
    } else {
        setFlag(Cpu.Flag.Z, result == 0)
    }
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rrcIndirect(location: Registers.Bit16) {
    val memorLocation = registers.get(location)
    val arg = memory.read(memorLocation)
    var result = (arg ushr 1).toUnsigned8BitInt()
    if (arg and 1 != 0) {
        result = result or (1 shl 7)
        setFlag(Cpu.Flag.C)
    } else {
        setFlag(Cpu.Flag.C, false)
    }
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    memory.write(memorLocation, result)
}

fun Cpu.rr(location: Registers.Bit8, forceZToOff: Boolean = false) {
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

    if (forceZToOff) {
        setFlag(Cpu.Flag.Z, false)
    } else {
        setFlag(Cpu.Flag.Z, result == 0)
    }
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)

    registers.set(location, result)
}

fun Cpu.rrIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)
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

    memory.write(memoryLocation, result)
}

fun Cpu.sla(location: Registers.Bit8) {
    val arg = registers.get(location)

    val result = (arg shl 1).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(7))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(location, result)
}

fun Cpu.slaIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)

    val result = (arg shl 1).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(7))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    memory.write(memoryLocation, result)
}

fun Cpu.sra(location: Registers.Bit8) {
    val arg = registers.get(location)
    val msb = arg.getBit(7)
    val result = ((arg ushr 1) or (msb shl 7)).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(0))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(location, result)
}

fun Cpu.sraIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)

    val msb = arg.getBit(7)
    val result = ((arg ushr 1) and (msb shl 7)).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(0))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    memory.write(memoryLocation, result)
}

fun Cpu.srl(location: Registers.Bit8) {
    val arg = registers.get(location)
    val result = ((arg ushr 1)).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(0))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(location, result)
}

fun Cpu.srlIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)

    val result = ((arg shr 1)).toUnsigned8BitInt()
    setFlag(Cpu.Flag.C, arg.checkBit(0))
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, result == 0)

    memory.write(memoryLocation, result)
}

fun Cpu.swap(location: Registers.Bit8) {
    val arg = registers.get(location)

    val lowerNibble = arg.getLowNibble()
    val higherNibble = arg.getHighNibble()
    // Swap the nibble and create a new value
    val result = higherNibble.toIntWithHighNibble(lowerNibble)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.C, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(location, result)
}

fun Cpu.swapIndirect(location: Registers.Bit16) {
    val memoryLocation = registers.get(location)
    val arg = memory.read(memoryLocation)

    val lowerNibble = arg.getLowNibble()
    val higherNibble = arg.getHighNibble()
    // Swap the nibble and create a new value
    val result = higherNibble.toIntWithHighNibble(lowerNibble)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.C, false)
    setFlag(Cpu.Flag.Z, result == 0)

    memory.write(memoryLocation, result)
}

// endregion