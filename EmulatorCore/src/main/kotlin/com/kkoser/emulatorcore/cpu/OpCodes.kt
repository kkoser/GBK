package com.kkoser.emulatorcore.cpu

import javax.swing.text.html.HTML.Tag.P

/**
 * An Operation can make changes to the Cpu
 *
 * @return The number of cycles this operation should take
 */
typealias Operator = (Cpu) -> Unit
open class Operation(val numBytes: Int,
                     val cycles: Int,
                     val title: String,
                     val operation: Operator)

object OpCodes {
    val opCodes = mapOf(
            0x00 to Operation(1, 4, "NOP", Cpu::noop),
            0x01 to Operation(3, 12, "LD BC,d16", { cpu -> cpu.loadImmediate16(Registers.Bit16.BC) }),
            0x02 to Operation(1, 8, "LD (BC),A", { cpu -> cpu.storeIndirect(Registers.Bit16.BC, Registers.Bit8.A)}),
            0x03 to Operation(1, 8, "INC BC", { cpu -> cpu.increment16(Registers.Bit16.BC)}),
            0x04 to Operation(1, 4, "INC B", { cpu -> cpu.increment8(Registers.Bit8.B)}),
            0x05 to Operation(1, 4, "DEC B", { cpu -> cpu.decrement8(Registers.Bit8.B)}),
            0x06 to Operation(2, 8, "LD B,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.B)})
    )
}