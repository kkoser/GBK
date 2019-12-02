package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toIntWithLowerInt
import com.kkoser.emulatorcore.toUnsigned16BitInt
import com.kkoser.emulatorcore.toUnsigned8BitInt

// A file to contain extensions for all the different Operations a cpu can take
// Using extensions allows these to be organzied into logical chunks and avoid becoming one massive file
fun Cpu.noop() {
    //no-op
}

// region Loads/Stores
fun Cpu.loadImmediate8(register: Registers.Bit8) {
    val value = memory.read(pc + 1)
    // Ensuring we never accidentally insert bad values into the registers is important, because we use the
    // setters for the registers as a catch-all to also set some flags
    if (value > Registers.MAX_VALUE_8) throw RuntimeException("Read an immediate value thats not a valid immediate 8 bit value")
    registers.set(register, value)
}

fun Cpu.loadImmediate16(register: Registers.Bit16) {
    val higher = memory.read(pc + 2)
    val lower = memory.read(pc + 1)
    registers.set(register, (higher shl 8) or lower)
}

val blah = mutableMapOf<String, String>()

val x: String? = null
val res = blah.remove(x)
val list: List<String> = listOf()
val uniques = list.distinctBy { it != "test" }

/**
 * Stores the value of the given register into the 16 bit address read following the opcode (pc+2)
 */
fun Cpu.storeIntoImmediateMemoryLocation(source: Registers.Bit16) {
    val location = memory.read(pc + 1).toIntWithLowerInt(memory.read(pc + 2))

    memory.write(location, registers.get(source))
}

fun Cpu.storeImmediateIntoIndirectMemoryLocation(source: Registers.Bit16) {
    memory.write(registers.get(source), memory.read(pc + 1))
}

fun Cpu.loadRegisterIntoIndirectLocation(location: Registers.Bit16, value: Registers.Bit8 ) {
    memory.write(registers.get(location), registers.get(value))
}

fun Cpu.loadIndirectValueIntoRegister(destination: Registers.Bit8, sourceAddress: Registers.Bit16) {
    registers.set(destination, memory.read(registers.get(sourceAddress)))
}

fun Cpu.loadRegisterIntoRegister(dest: Registers.Bit8, source: Registers.Bit8) {
    registers.set(dest, registers.get(source))
}

fun Cpu.loadRegisterIntoRegister(dest: Registers.Bit16, source: Registers.Bit16) {
    registers.set(dest, registers.get(source))
}

fun Cpu.loadRegisterIntoIndirectLocationAndIncrement(location: Registers.Bit16, value: Registers.Bit8 ) {
    memory.write(registers.get(location), registers.get(value))
    increment16(location)
}

fun Cpu.loadRegisterIntoIndirectLocationAndDecrement(location: Registers.Bit16, value: Registers.Bit8 ) {
    memory.write(registers.get(location), registers.get(value))
    decrement16(location)
}

fun Cpu.loadIndirectValueIntoRegisterAndIncrement(destination: Registers.Bit8, sourceLocation: Registers.Bit16) {
    registers.set(destination, memory.read(registers.get(sourceLocation)))
    increment16(sourceLocation)
}

fun Cpu.loadIndirectValueIntoRegisterAndDecrement(destination: Registers.Bit8, sourceLocation: Registers.Bit16) {
    registers.set(destination, memory.read(registers.get(sourceLocation)))
    decrement16(sourceLocation)
}

fun Cpu.loadRegisterIntoImmediateLocation(valueRegister: Registers.Bit8) {
    val lower = memory.read(pc + 1)
    val higher = memory.read(pc + 2)
    val location = ((higher shl 8) or lower).toUnsigned16BitInt()
    memory.write(location, registers.get(valueRegister))
}

fun Cpu.loadImmediateLocationIntoRegister(register: Registers.Bit8) {
    val higher = memory.read(pc + 1)
    val lower = memory.read(pc + 2)
    val location = ((higher shl 8) or lower).toUnsigned16BitInt()
    registers.set(register, memory.read(location))
}

val LDH_OFFSET = 0xFF00

fun Cpu.ldhRegisterValueIntoImmediate() {
    val location = memory.read(pc + 1) + LDH_OFFSET
    memory.write(location, registers.get(Registers.Bit8.A))
}

fun Cpu.ldhImmediateMemoryLocationIntoRegister() {
    val locationToReadFrom = memory.read(pc + 1).toUnsigned8BitInt() + LDH_OFFSET
    registers.set(Registers.Bit8.A, memory.read(locationToReadFrom))
}

fun Cpu.loadIndirectRegisterValueIntoRegisterA(register: Registers.Bit8) {
    val location = registers.get(register) + LDH_OFFSET
    registers.set(Registers.Bit8.A, memory.read(location))
}

fun Cpu.loadRegisterAValueIntoIndirectRegisterLocation(register: Registers.Bit8) {
    val location = registers.get(register) + LDH_OFFSET
    memory.write(location, registers.get(Registers.Bit8.A))
}

fun Cpu.loadRegisterWithImmediateOffsetIntoRegister(destination: Registers.Bit16, source: Registers.Bit16) {
    val oldVal = registers.get(source)
    val offset = memory.readSigned(pc + 1)
    val result = oldVal + offset
    setFlag(Cpu.Flag.H, check8BitCarry(oldVal, offset.toInt()))
    setFlag(Cpu.Flag.C, result > 0xFFFF)

    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.Z, false)

    registers.set(destination, result.toUnsigned16BitInt())
}

// endregion


// Region ALU

fun Cpu.increment8(location: Registers.Bit8) {
    setFlag(Cpu.Flag.N, false)
    var value = registers.get(location) + 1
    // Actually wrap around
    value = if (value > Registers.MAX_VALUE_8) value % Registers.MAX_VALUE_8 else value

    setFlag(Cpu.Flag.H, check8BitCarry(registers.get(location), 1))
    setFlag(Cpu.Flag.Z, value == 0)
    registers.set(location, value)
}

fun Cpu.decrement8(location: Registers.Bit8) {
    setFlag(Cpu.Flag.N, true)
    var value = registers.get(location) - 1
    // Actually wrap around
    value = if (value > Registers.MAX_VALUE_8) value % Registers.MAX_VALUE_8 else value

    setFlag(Cpu.Flag.H, check8BitCarry(registers.get(location), -1))
    setFlag(Cpu.Flag.Z, value == 0)
    registers.set(location, value)
}

// NOTE: 16-bit inc/dec don't modify any flags
fun Cpu.increment16(location: Registers.Bit16) {
    registers.set(location, registers.get(location) + 1)
}

fun Cpu.decrement16(location: Registers.Bit16) {
    registers.set(location, registers.get(location) - 1)
}

fun Cpu.incrementMemory(location: Registers.Bit16) {
    val value = memory.read(registers.get(location))
    val result = value + 1
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.H, check8BitCarry(value, 1))
    setFlag(Cpu.Flag.N, false)

    memory.write(registers.get(location), result)
}

fun Cpu.decrementMemory(location: Registers.Bit16) {
    val value = memory.read(registers.get(location))
    val result = value - 1
    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.H, check8BitCarry(value, -1))
    setFlag(Cpu.Flag.N, true)

    memory.write(registers.get(location), result)
}

fun Cpu.add8Value(arg1: Int, arg2: Int, storeTo: Registers.Bit8) {
    val result = (arg1 + arg2).toUnsigned8BitInt()
    setFlag(Cpu.Flag.H, check8BitCarry(arg1, arg2))
    setFlag(Cpu.Flag.C, (arg1 + arg2 - 0xff) > 0)

    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(storeTo, result)
}

fun Cpu.add8(x: Registers.Bit8, y: Registers.Bit8) {
    add8Value(registers.get(x), registers.get(y), x)
}

fun Cpu.add8Indirect(x: Registers.Bit8, sourceLocation: Registers.Bit16) {
    add8Value(registers.get(x), memory.read(registers.get(sourceLocation)), x)
}

fun Cpu.add8Immediate() {
    add8Value(registers.get(Registers.Bit8.A), memory.read(pc + 1), Registers.Bit8.A)
}

fun Cpu.add8ImmediateToSp() {
    val sp = registers.get(Registers.Bit16.SP)
    val immediate = memory.readSigned(pc + 1).toByte()
    val result = (sp + immediate).toUnsigned16BitInt()
    setFlag(Cpu.Flag.H, check8BitCarry(sp, immediate.toInt()))
    setFlag(Cpu.Flag.C, sp + immediate > 0xffff)

    setFlag(Cpu.Flag.Z, false)
    setFlag(Cpu.Flag.N, false)

    registers.set(Registers.Bit16.SP, result)

}

fun Cpu.adcValue(value: Int) {
    val arg1 = registers.get(Registers.Bit8.A)
    val carryVal = if (checkFlag(Cpu.Flag.C)) 1 else 0
    val result = arg1 + value + carryVal

    setFlag(Cpu.Flag.H, check8BitCarry(arg1, value))
    setFlag(Cpu.Flag.C, arg1 + value > 0xffff)

    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(Registers.Bit8.A, result)
}

fun Cpu.adc(x: Registers.Bit8) {
    adcValue(registers.get(x))
}

fun Cpu.adcIndirect(sourceLocation: Registers.Bit16) {
    adcValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.adcImmediate() {
    adcValue(memory.read(pc + 1))
}

fun Cpu.sub8Value(arg1: Int, arg2: Int, storeTo: Registers.Bit8) {
    val result = (arg1 - arg2).toUnsigned8BitInt()
    setFlag(Cpu.Flag.H, check8BitCarry(arg1, arg2))
    setFlag(Cpu.Flag.C, arg1 + arg2 > 0xffff)

    setFlag(Cpu.Flag.N, true)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(storeTo, result)
}

fun Cpu.sub8(x: Registers.Bit8, y: Registers.Bit8) {
    sub8Value(registers.get(x), registers.get(y), x)
}

fun Cpu.sub8Indirect(x: Registers.Bit8, sourceLocation: Registers.Bit16) {
    sub8Value(registers.get(x), memory.read(registers.get(sourceLocation)), x)
}

fun Cpu.sub8Immediate() {
    sub8Value(registers.get(Registers.Bit8.A), memory.read(pc + 1), Registers.Bit8.A)
}

fun Cpu.sbcValue(value: Int) {
    val arg1 = registers.get(Registers.Bit8.A)
    val carryVal = if (checkFlag(Cpu.Flag.C)) 1 else 0
    val result = arg1 - value - carryVal

    setFlag(Cpu.Flag.H, check8BitCarry(arg1, value))
    setFlag(Cpu.Flag.C, arg1 - value < 0)

    setFlag(Cpu.Flag.N, true)
    setFlag(Cpu.Flag.Z, result == 0)

    registers.set(Registers.Bit8.A, result)
}

fun Cpu.sbc(x: Registers.Bit8) {
    sbcValue(registers.get(x))
}

fun Cpu.sbcIndirect(sourceLocation: Registers.Bit16) {
    sbcValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.sbcImmediate() {
    sbcValue(memory.read(pc + 1))
}

fun Cpu.add16(x: Registers.Bit16, y: Registers.Bit16) {
    val arg1 = registers.get(x)
    val arg2 = registers.get(y)
    setFlag(Cpu.Flag.H, (arg1 and 0x0fff) + (arg2 and 0x0fff) > 0x0fff)
    setFlag(Cpu.Flag.C, arg1 + arg2 > 0xffff)

    // 16 bit adds do not set the Zero flag
    setFlag(Cpu.Flag.N, false)

    registers.set(x, (arg1 + arg2).toUnsigned16BitInt())
}

// endregion

// region Logical Ops

fun Cpu.andValue(value: Int) {
    val result = (registers.get(Registers.Bit8.A) and value).toUnsigned8BitInt()

    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H)
    setFlag(Cpu.Flag.C, false)

    registers.set(Registers.Bit8.A, value)
}

fun Cpu.and(x: Registers.Bit8) {
    andValue(registers.get(x))
}

fun Cpu.andIndirect(sourceLocation: Registers.Bit16) {
    andValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.andImmediate() {
    andValue(memory.read(pc + 1))
}

fun Cpu.xorValue(value: Int) {
    val result = (registers.get(Registers.Bit8.A) xor value).toUnsigned8BitInt()

    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.C, false)

    registers.set(Registers.Bit8.A, result)
}

fun Cpu.xor(x: Registers.Bit8) {
    xorValue(registers.get(x))
}

fun Cpu.xorIndirect(sourceLocation: Registers.Bit16) {
    xorValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.xor8Immediate() {
    xorValue(memory.read(pc + 1))
}

fun Cpu.orValue(value: Int) {
    val result = (registers.get(Registers.Bit8.A) or value).toUnsigned8BitInt()

    setFlag(Cpu.Flag.Z, result == 0)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.C, false)

    registers.set(Registers.Bit8.A, value)
}

fun Cpu.or(x: Registers.Bit8) {
    orValue(registers.get(x))
}

fun Cpu.orIndirect(sourceLocation: Registers.Bit16) {
    orValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.or8Immediate() {
    orValue(memory.read(pc + 1))
}

// From the docs: "This is basically an A - n subtraction instruction but the results are thrown away."
fun Cpu.compareValue(value: Int) {
    // Use the special UNUSED register so that we can just throw the result away, but still set the flags
    sub8Value(registers.get(Registers.Bit8.A), value, Registers.Bit8.UNUSED)
}

fun Cpu.compare(x: Registers.Bit8) {
    compareValue(registers.get(x))
}

fun Cpu.compareIndirect(sourceLocation: Registers.Bit16) {
    compareValue(memory.read(registers.get(sourceLocation)))
}

fun Cpu.compareImmediate() {
    compareValue(memory.read(pc + 1))
}

fun Cpu.complementA() {
    registers.set(Registers.Bit8.A, registers.get(Registers.Bit8.A).inv())
    setFlag(Cpu.Flag.N)
    setFlag(Cpu.Flag.H)
}

// endregion

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
    val arg = registers.get(location)
    var result = (arg shl 1).toUnsigned8BitInt() and 0x01
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

// Borrowed from https://github.com/kotcrab/xgbc/blob/master/src/main/kotlin/com/kotcrab/xgbc/cpu/OpCodesProcessor.kt
// I'm not super sure what this one does
fun Cpu.daa() {
    var regA = registers.get(Registers.Bit8.A)

    if (checkFlag(Cpu.Flag.N) == false) {
        if (checkFlag(Cpu.Flag.H) || (regA and 0xF) > 9) regA += 0x06
        if (checkFlag(Cpu.Flag.C) || regA > 0x9F) regA += 0x60
    } else {
        if (checkFlag(Cpu.Flag.H)) regA = (regA - 6) and 0xFF
        if (checkFlag(Cpu.Flag.C)) regA -= 0x60
    }

    setFlag(Cpu.Flag.H, false)
    setFlag(Cpu.Flag.Z, false)
    if ((regA and 0x100) == 0x100) {
        setFlag(Cpu.Flag.C)
    }

    regA = regA and 0xFF

    if (regA == 0) setFlag(Cpu.Flag.Z)

    registers.set(Registers.Bit8.A, regA)
}

// endregion

// region Flow Control

fun Cpu.jumpRelative() {
    // We add 2 to this, since the jump command is 2 bytes long itself (and counts)
    val offset = memory.readSigned(pc + 1).toByte() + 2
    pc += offset
}

fun Cpu.jumpRelativeFlag(flag: Cpu.Flag, expectedFlagValue: Boolean) {
    if (checkFlag(flag) == expectedFlagValue) {
        jumpRelative()
    }
}

fun Cpu.jumpImmediate() {
    val location = memory.read(pc + 2).toIntWithLowerInt(memory.read(pc + 1))
    pc = location
}

fun Cpu.jumpImmediateFlag(flag: Cpu.Flag, expectedFlagValue: Boolean) {
    if (checkFlag(flag) == expectedFlagValue) {
        jumpImmediate()
    }
}

fun Cpu.jumpRegister(register: Registers.Bit16) {
    val location = registers.get(register)
    pc = location
}

fun Cpu.popStack(): Int {
    val pcl = memory.read(registers.get(Registers.Bit16.SP))
    val pch = memory.read(registers.get(Registers.Bit16.SP) + 1)

    // move the stack back one address (so 2 bytes)
    registers.set(Registers.Bit16.SP, registers.get(Registers.Bit16.SP) + 2)

    return pch.toIntWithLowerInt(pcl).toUnsigned16BitInt()
}

fun Cpu.popInto(destination: Registers.Bit16) {
    registers.set(destination, popStack())
}

fun Cpu.push(value: Int) {
    val high = (value and 0xFF00) shr 8
    val low = (value and 0x00FF)
    val sp = registers.get(Registers.Bit16.SP)

    memory.write(sp - 1, high)
    memory.write(sp - 2, low)
    registers.set(Registers.Bit16.SP, sp - 2)
}

fun Cpu.pushRegister(register: Registers.Bit16) {
    push(registers.get(register))
}

fun Cpu.ret() {
    pc = popStack()
}

fun Cpu.retFlag(flag: Cpu.Flag, expectedFlagValue: Boolean) {
    if (checkFlag(flag) == expectedFlagValue) {
        ret()
    }
}

fun Cpu.call(location: Int) {
    System.out.println("Calling from pc ${pc.toHexString()} to location ${location.toHexString()}")
    push(pc + 3)
    pc = location.toUnsigned16BitInt()
}

fun Cpu.callImmediate() {
    val lower = memory.read(pc + 1)
    val higher = memory.read(pc + 2)
    val location = ((higher shl 8) or lower)

    call(location)
}

fun Cpu.callImmediateFlag(flag: Cpu.Flag, expectedFlagValue: Boolean) {
    if (checkFlag(flag) == expectedFlagValue) {
        callImmediate()
    }
}

fun Cpu.reset(location: Int) {
    push(pc+1)
    pc = location
}

fun Cpu.halt() {
    halted = true
}

// endregion

// Shoutout to https://robdor.com/2016/08/10/gameboy-emulator-half-carry-flag/ for a great explanation of this algorithm
fun check8BitCarry(a: Int, b: Int): Boolean {
    return (((a and 0xf) + (b and 0xf)) and 0x10) == 0x10
}