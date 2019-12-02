package com.kkoser.emulatorcore.cpu

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

    return (lower shl 4) and (higher shr 4);
}