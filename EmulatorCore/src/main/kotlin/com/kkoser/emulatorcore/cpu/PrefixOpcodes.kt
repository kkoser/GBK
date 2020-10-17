package com.kkoser.emulatorcore.cpu

import com.kkoser.emulatorcore.toHexString

object PrefixOpcodes {
    // This could (probably should) be converted to an intArray in the future for better performance. I did it this way
    // During development to make it easier to read what maps to what
    // Based on http://www.pastraiser.com/cpu/gameboy/gameboy_opcodes.html
    val invalid = Operation(0, 0, "BAD OPCODE", { cpu -> throw RuntimeException("BAD OPCODE") })
    val notImplemented: (Cpu)-> Unit =  { cpu -> println("cb unimpl ${cpu.pc.toHexString()}"); throw RuntimeException("BAD OPCODE") }
    val registers8Bit = arrayOf(Registers.Bit8.B, Registers.Bit8.C, Registers.Bit8.D, Registers.Bit8.E, Registers.Bit8.H, Registers.Bit8.L)

    val opCodes = mutableMapOf<Int, Operation>(

//            0x30 to Operation(2, 8, "SWAP B", { cpu-> cpu.swapImmediate(Registers.Bit8.B) }, true, 8),
//            0x31 to Operation(2, 8, "SWAP C", { cpu-> cpu.swapImmediate(Registers.Bit8.C) }),
//            0x32 to Operation(2, 8, "SWAP D", { cpu-> cpu.swapImmediate(Registers.Bit8.D) }),
//            0x33 to Operation(2, 8, "SWAP E", { cpu-> cpu.swapImmediate(Registers.Bit8.E) }),
//            0x34 to Operation(2, 8, "SWAP H", { cpu-> cpu.swapImmediate(Registers.Bit8.H) }),
//            0x35 to Operation(2, 8, "SWAP L", { cpu-> cpu.swapImmediate(Registers.Bit8.L) }),
//            0x36 to Operation(2, 16, "SWAP (HL)", { cpu-> cpu.swapIndirect(Registers.Bit16.HL) }),
//            0x37 to Operation(2, 8, "SWAP A", { cpu-> cpu.swapImmediate(Registers.Bit8.A) }),
//            0x38 to Operation(2, 12, "JR C,r8", notImplemented, true, 8),
//            0x39 to Operation(1, 8, "ADD HL,SP", notImplemented),
//            0x3A to Operation(1, 8, "LD A,(HL-)", notImplemented),
//            0x3B to Operation(1, 8, "DEC SP", notImplemented),
//            0x3C to Operation(1, 4, "INC A", notImplemented),
//            0x3D to Operation(1, 4, "DEC A", notImplemented),
//            0x3E to Operation(2, 8, "LD A,d8", notImplemented),
//            0x3F to Operation(1, 4, "CCF", notImplemented),
//
//            0x40 to Operation(1, 4, "BIT 0,B", { cpu -> cpu.checkBit(Registers.Bit8.B, 0) }),
//            0x41 to Operation(1, 4, "BIT 0,C", { cpu -> cpu.checkBit(Registers.Bit8.C, 0) }),
//            0x42 to Operation(1, 4, "BIT 0,D", { cpu -> cpu.checkBit(Registers.Bit8.D, 0) }),
//            0x43 to Operation(1, 4, "BIT 0,E", { cpu -> cpu.checkBit(Registers.Bit8.E, 0) }),
//            0x44 to Operation(1, 4, "BIT 0,H", { cpu -> cpu.checkBit(Registers.Bit8.H, 0) }),
//            0x45 to Operation(1, 4, "BIT 0,L", { cpu -> cpu.checkBit(Registers.Bit8.L, 0) }),
//
//            0x70 to Operation(1, 8, "LD (HL),B", notImplemented),
//            0x71 to Operation(1, 8, "LD (HL),C", notImplemented),
//            0x72 to Operation(1, 8, "LD (HL),D", notImplemented),
//            0x73 to Operation(1, 8, "LD (HL),E", notImplemented),
//            0x74 to Operation(1, 8, "LD (HL),H", notImplemented),
//            0x75 to Operation(1, 8, "LD (HL),L", notImplemented),
//            0x76 to Operation(1, 4, "HALT", notImplemented),
//            0x77 to Operation(1, 8, "LD (HL),A", notImplemented),
//            0x78 to Operation(1, 4, "LD A,B", notImplemented),
//            0x79 to Operation(1, 4, "LD A,C", notImplemented),
//            0x7A to Operation(1, 4, "LD A,D", notImplemented),
//            0x7B to Operation(1, 4, "LD A,E", notImplemented),
//            0x7C to Operation(2, 8, "BIT 7, H", { cpu -> cpu.checkBit(Registers.Bit8.H, 7) }),
//            0x7D to Operation(1, 4, "LD A,L", notImplemented),
//            0x7E to Operation(1, 8, "LD A,(HL)", notImplemented),
//            0x7F to Operation(1, 4, "LD A,A", notImplemented)

    ).apply {
        // RLC
        for ((index, register) in registers8Bit.withIndex()) {
            set(index, Operation(2, 8, "RLC ${register.name}", { cpu -> cpu.rlc(register)}))
        }
        set(0x06, Operation(2, 16, "RLC (HL)", { cpu -> cpu.rlcIndirect(Registers.Bit16.HL)}))
        set(0x07, Operation(2, 8, "RLC A", { cpu -> cpu.rlc(Registers.Bit8.A)}))

        // RRC
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x08, Operation(2, 8, "RRC ${register.name}", { cpu -> cpu.rrc(register)}))
        }
        set(0xE, Operation(2, 16, "RRC (HL)", { cpu -> cpu.rrcIndirect(Registers.Bit16.HL)}))
        set(0xF, Operation(2, 8, "RRC A", { cpu -> cpu.rrc(Registers.Bit8.A)}))

        // RL
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x10, Operation(2, 8, "RL ${register.name}", { cpu -> cpu.rl(register)}))
        }
        set(0x16, Operation(2, 16, "RL (HL)", { cpu -> cpu.rlIndirect(Registers.Bit16.HL)}))
        set(0x17, Operation(2, 8, "RL A", { cpu -> cpu.rl(Registers.Bit8.A)}))

        // RR
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x18, Operation(2, 8, "RR ${register.name}", { cpu -> cpu.rr(register)}))
        }
        set(0x1E, Operation(2, 16, "RR (HL)", { cpu -> cpu.rrIndirect(Registers.Bit16.HL)}))
        set(0x1F, Operation(2, 8, "RR A", { cpu -> cpu.rr(Registers.Bit8.A)}))

        // SLA
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x20, Operation(2, 8, "SLA ${register.name}", { cpu -> cpu.sla(register)}))
        }
        set(0x26, Operation(2, 16, "SLA (HL)", { cpu -> cpu.slaIndirect(Registers.Bit16.HL)}))
        set(0x27, Operation(2, 8, "SLA A", { cpu -> cpu.sla(Registers.Bit8.A)}))

        // SRA
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x28, Operation(2, 8, "SRA ${register.name}", { cpu -> cpu.sra(register)}))
        }
        set(0x2E, Operation(2, 16, "SRA (HL)", { cpu -> cpu.sraIndirect(Registers.Bit16.HL)}))
        set(0x2F, Operation(2, 8, "SRA A", { cpu -> cpu.sra(Registers.Bit8.A)}))

        // SWAP
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x30, Operation(2, 8, "SWAP ${register.name}", { cpu -> cpu.swap(register)}))
        }
        set(0x36, Operation(2, 16, "SWAP (HL)", { cpu -> cpu.swapIndirect(Registers.Bit16.HL)}))
        set(0x37, Operation(2, 8, "SWAP A", { cpu -> cpu.swap(Registers.Bit8.A)}))

        // SRL
        for ((index, register) in registers8Bit.withIndex()) {
            set(index + 0x38, Operation(2, 8, "SRL ${register.name}", { cpu -> cpu.srl(register)}))
        }
        set(0x3E, Operation(2, 16, "SRL (HL)", { cpu -> cpu.srlIndirect(Registers.Bit16.HL)}))
        set(0x3F, Operation(2, 8, "SRL A", { cpu -> cpu.srl(Registers.Bit8.A)}))

        // BIT X, N
        val bitNumbers = arrayOf(0,1,2,3,4,5,6,7)
        val bitStartOffset = 0x40

        for ((index, bitNumber) in bitNumbers.withIndex()) {
            for ((regIndex, register) in registers8Bit.withIndex()) {
                set(bitStartOffset + (bitNumbers.size * index) + regIndex, Operation(2, 8, "BIT $bitNumber ${register.name}", { cpu -> cpu.checkBit(register, bitNumber)}))
            }
            set(bitStartOffset + (bitNumbers.size * index) + registers8Bit.size, Operation(2, 16, "BIT $bitNumber, (HL)", { cpu -> cpu.checkBitIndirect(Registers.Bit16.HL, bitNumber)}))
            set(bitStartOffset + (bitNumbers.size * index) + registers8Bit.size + 1, Operation(2, 8, "BIT $bitNumber, A", { cpu -> cpu.checkBit(Registers.Bit8.A, bitNumber)}))
        }

        // RES X, N
        val resStartOffset = 0x80
        for ((index, bitNumber) in bitNumbers.withIndex()) {
            for ((regIndex, register) in registers8Bit.withIndex()) {
                set(resStartOffset + (bitNumbers.size * index) + regIndex, Operation(2, 8, "RES $bitNumber ${register.name}", { cpu -> cpu.resetBit(register, bitNumber)}))
            }
            set(resStartOffset + (bitNumbers.size * index) + registers8Bit.size, Operation(2, 16, "RES $bitNumber, (HL)", { cpu -> cpu.resetBitIndirect(Registers.Bit16.HL, bitNumber)}))
            set(resStartOffset + (bitNumbers.size * index) + registers8Bit.size + 1, Operation(2, 8, "RES $bitNumber, A", { cpu -> cpu.resetBit(Registers.Bit8.A, bitNumber)}))
        }

        // SET X, N
        val setStartOffset = 0xC0
        for ((index, bitNumber) in bitNumbers.withIndex()) {
            for ((regIndex, register) in registers8Bit.withIndex()) {
                set(setStartOffset + (bitNumbers.size * index) + regIndex, Operation(2, 8, "SET $bitNumber ${register.name}", { cpu -> cpu.setBit(register, bitNumber)}))
            }
            set(setStartOffset + (bitNumbers.size * index) + registers8Bit.size, Operation(2, 16, "SET $bitNumber, (HL)", { cpu -> cpu.setBitIndirect(Registers.Bit16.HL, bitNumber)}))
            set(setStartOffset + (bitNumbers.size * index) + registers8Bit.size + 1, Operation(2, 8, "SET $bitNumber, A", { cpu -> cpu.setBit(Registers.Bit8.A, bitNumber)}))
        }



        assert(keys.size == 0xFF)
    }
}