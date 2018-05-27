package com.kkoser.emulatorcore.cpu

// A file to contain extensions for all the different Operations a cpu can take
// Using extensions allows these to be organzied into logical chunks and avoid becoming one massive file
fun Cpu.noop() {
    //no-op
}

fun Cpu.loadImmediate8(register: Registers.Bit8) {
    val value = memory.read(pc + 1)
    // Ensuring we never accidentally insert bad values into the registers is important, because we use the
    // setters for the registers as a catch-all to also set some flags
    if (value > Registers.MAX_VALUE) throw RuntimeException("Read an immediate value thats not a valid immediate 8 bit value")
    registers.set(register, value)
}

fun Cpu.loadImmediate16(register: Registers.Bit16) {
    val higher = memory.read(pc + 1)
    val lower = memory.read(pc + 2)
    registers.set(register, (higher shl 8) or lower)
}

fun Cpu.storeIndirect(location: Registers.Bit16, value: Registers.Bit8 ) {
    memory.write(registers.get(location), registers.get(value))
}

fun Cpu.increment8(location: Registers.Bit8) {
    setFlag(Cpu.Flag.N, false)
    registers.set(location, registers.get(location) + 1)
}

fun Cpu.increment16(location: Registers.Bit16) {
    setFlag(Cpu.Flag.N, false)
    registers.set(location, registers.get(location) + 1)
}

fun Cpu.decrement8(location: Registers.Bit8) {
    setFlag(Cpu.Flag.N, true)
    registers.set(location, registers.get(location) - 1)
}

fun Cpu.decrement16(location: Registers.Bit16) {
    setFlag(Cpu.Flag.N, true)
    registers.set(location, registers.get(location) - 1)
}

fun Cpu.rlc(location: Registers.Bit8) {
    setFlag(Cpu.Flag.Z, false)
    setFlag(Cpu.Flag.N, false)
    setFlag(Cpu.Flag.H, false)
//    val topBit =
}