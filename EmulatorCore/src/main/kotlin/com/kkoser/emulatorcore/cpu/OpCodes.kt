package com.kkoser.emulatorcore.cpu

import java.util.logging.Level
import java.util.logging.Logger

/**
 * An Operation can make changes to the Cpu
 *
 * @return The number of cycles this operation should take
 */
typealias Operator = (Cpu) -> Unit

open class Operation(val numBytes: Int,
                     val cycles: Int,
                     val title: String,
                     val operation: Operator,
                     val isJump: Boolean = false,
                     val notTakenCycles: Int = 0)

object OpCodes {
    // This could (probably should) be converted to an intArray in the future for better performance. I did it this way
    // During development to make it easier to read what maps to what
    // Based on http://www.pastraiser.com/cpu/gameboy/gameboy_opcodes.html
    val invalid = Operation(0, 0, "BAD OPCODE", { cpu -> throw RuntimeException("BAD OPCODE") })

    val opCodes = mapOf(
            0x00 to Operation(1, 4, "NOP", Cpu::noop),
            0x01 to Operation(3, 12, "LD BC,d16", { cpu -> cpu.loadImmediate16(Registers.Bit16.BC) }),
            0x02 to Operation(1, 8, "LD (BC),A", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.BC, Registers.Bit8.A) }),
            0x03 to Operation(1, 8, "INC BC", { cpu -> cpu.increment16(Registers.Bit16.BC) }),
            0x04 to Operation(1, 4, "INC B", { cpu -> cpu.increment8(Registers.Bit8.B) }),
            0x05 to Operation(1, 4, "DEC B", { cpu -> cpu.decrement8(Registers.Bit8.B) }),
            0x06 to Operation(2, 8, "LD B,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.B) }),
            0x07 to Operation(1, 4, "RLCA", { cpu -> cpu.rlc(Registers.Bit8.A) }),
            0x08 to Operation(3, 20, "LD (a16),SP", { cpu -> cpu.storeIntoImmediateMemoryLocation(Registers.Bit16.SP) }),
            0x09 to Operation(1, 8, "ADD HL,BC", { cpu -> cpu.add16(Registers.Bit16.HL, Registers.Bit16.BC) }),
            0x0A to Operation(1, 8, "LD A,(BC)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.A, Registers.Bit16.BC) }),
            0x0B to Operation(1, 8, "DEC BC", { cpu -> cpu.decrement16(Registers.Bit16.BC) }),
            0x0C to Operation(1, 4, "INC C", { cpu -> cpu.increment8(Registers.Bit8.C) }),
            0x0D to Operation(1, 4, "DEC C", { cpu -> cpu.decrement8(Registers.Bit8.C) }),
            0x0E to Operation(2, 8, "LD C,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.C) }),
            0x0F to Operation(1, 4, "RRCA", { cpu -> cpu.rrc(Registers.Bit8.A) }),

            0x10 to Operation(2, 4, "STOP 0", { cpu -> throw RuntimeException("STOP Command! Idk what to do here") }),
            0x11 to Operation(3, 12, "LD DE,d16", { cpu -> cpu.loadImmediate16(Registers.Bit16.DE) }),
            0x12 to Operation(1, 8, "LD (DE),A", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.DE, Registers.Bit8.A) }),
            0x13 to Operation(1, 8, "INC DE", { cpu -> cpu.increment16(Registers.Bit16.DE) }),
            0x14 to Operation(1, 4, "INC D", { cpu -> cpu.increment8(Registers.Bit8.D) }),
            0x15 to Operation(1, 4, "DEC D", { cpu -> cpu.decrement8(Registers.Bit8.D) }),
            0x16 to Operation(2, 8, "LD D,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.D) }),
            0x17 to Operation(1, 4, "RLA", { cpu -> cpu.rl(Registers.Bit8.A) }),
            0x18 to Operation(2, 12, "JR r8", { cpu -> cpu.jumpRelative() }, true),
            0x19 to Operation(1, 8, "ADD HL,DE", { cpu -> cpu.add16(Registers.Bit16.HL, Registers.Bit16.DE) }),
            0x1A to Operation(1, 8, "LD A,(DE)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.A, Registers.Bit16.DE) }),
            0x1B to Operation(1, 8, "DEC DE", { cpu -> cpu.decrement16(Registers.Bit16.DE) }),
            0x1C to Operation(1, 4, "INC E", { cpu -> cpu.increment8(Registers.Bit8.E) }),
            0x1D to Operation(1, 4, "DEC E", { cpu -> cpu.decrement8(Registers.Bit8.E) }),
            0x1E to Operation(2, 8, "LD E,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.E) }),
            0x1F to Operation(1, 4, "RRA", { cpu -> cpu.rr(Registers.Bit8.A) }),

            0x20 to Operation(2, 12, "JR NZ,r8", { cpu -> cpu.jumpRelativeFlag(Cpu.Flag.Z, false) }, true, 8),
            0x21 to Operation(3, 12, "LD HL,d16", { cpu -> cpu.loadImmediate16(Registers.Bit16.HL) }),
            0x22 to Operation(1, 8, "LD (HL+),A", { cpu -> cpu.loadRegisterIntoIndirectLocationAndIncrement(Registers.Bit16.HL, Registers.Bit8.A) }),
            0x23 to Operation(1, 8, "INC HL", { cpu -> cpu.increment16(Registers.Bit16.HL) }),
            0x24 to Operation(1, 4, "INC H", { cpu -> cpu.increment8(Registers.Bit8.H) }),
            0x25 to Operation(1, 4, "DEC H", { cpu -> cpu.decrement8(Registers.Bit8.H) }),
            0x26 to Operation(2, 8, "LD H,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.H) }),
            0x27 to Operation(1, 4, "DAA", { cpu -> cpu.daa() }),
            0x28 to Operation(2, 12, "JR Z,r8", { cpu -> cpu.jumpRelativeFlag(Cpu.Flag.Z, true) }, true, 8),
            0x29 to Operation(1, 8, "ADD HL,HL", { cpu -> cpu.add16(Registers.Bit16.HL, Registers.Bit16.HL) }),
            0x2A to Operation(1, 8, "LD A,(HL+)", { cpu -> cpu.loadIndirectValueIntoRegisterAndIncrement(Registers.Bit8.A, Registers.Bit16.HL) }),
            0x2B to Operation(1, 8, "DEC HL", { cpu -> cpu.decrement16(Registers.Bit16.HL) }),
            0x2C to Operation(1, 4, "INC L", { cpu -> cpu.increment8(Registers.Bit8.L) }),
            0x2D to Operation(1, 4, "DEC L", { cpu -> cpu.decrement8(Registers.Bit8.L) }),
            0x2E to Operation(2, 8, "LD L,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.L) }),
            0x2F to Operation(1, 4, "CPL", { cpu -> cpu.complementA() }),

            0x30 to Operation(2, 12, "JR NC,r8", { cpu -> cpu.jumpRelativeFlag(Cpu.Flag.C, false) }, true, 8),
            0x31 to Operation(3, 12, "LD SP,d16", { cpu -> cpu.loadImmediate16(Registers.Bit16.SP) }),
            0x32 to Operation(1, 8, "LD (HL-),A", { cpu -> cpu.loadRegisterIntoIndirectLocationAndDecrement(Registers.Bit16.HL, Registers.Bit8.A) }),
            0x33 to Operation(1, 8, "INC SP", { cpu -> cpu.increment16(Registers.Bit16.SP) }),
            0x34 to Operation(1, 12, "INC (HL)", { cpu -> cpu.incrementMemory(Registers.Bit16.HL) }),
            0x35 to Operation(1, 12, "DEC (HL)", { cpu -> cpu.decrementMemory(Registers.Bit16.HL) }),
            0x36 to Operation(2, 12, "LD (HL),d8", { cpu -> cpu.storeImmediateIntoIndirectMemoryLocation(Registers.Bit16.HL) }),
            0x37 to Operation(1, 4, "SCF", { cpu -> cpu.setFlag(Cpu.Flag.C, true) }),
            0x38 to Operation(2, 12, "JR C,r8", { cpu -> cpu.jumpRelativeFlag(Cpu.Flag.C, true) }, true, 8),
            0x39 to Operation(1, 8, "ADD HL,SP", { cpu -> cpu.add16(Registers.Bit16.HL, Registers.Bit16.SP) }),
            0x3A to Operation(1, 8, "LD A,(HL-)", { cpu -> cpu.loadIndirectValueIntoRegisterAndDecrement(Registers.Bit8.A, Registers.Bit16.HL) }),
            0x3B to Operation(1, 8, "DEC SP", { cpu -> cpu.decrement16(Registers.Bit16.SP) }),
            0x3C to Operation(1, 4, "INC A", { cpu -> cpu.increment8(Registers.Bit8.A) }),
            0x3D to Operation(1, 4, "DEC A", { cpu -> cpu.decrement8(Registers.Bit8.A) }),
            0x3E to Operation(2, 8, "LD A,d8", { cpu -> cpu.loadImmediate8(Registers.Bit8.A) }),
            0x3F to Operation(1, 4, "CCF", { cpu -> cpu.setFlag(Cpu.Flag.C, false) }),

            0x40 to Operation(1, 4, "LD B,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.B) }),
            0x41 to Operation(1, 4, "LD B,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.C) }),
            0x42 to Operation(1, 4, "LD B,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.D) }),
            0x43 to Operation(1, 4, "LD B,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.E) }),
            0x44 to Operation(1, 4, "LD B,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.H) }),
            0x45 to Operation(1, 4, "LD B,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.L) }),
            0x46 to Operation(1, 8, "LD B,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.B, Registers.Bit16.HL) }),
            0x47 to Operation(1, 4, "LD B,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.B, Registers.Bit8.A) }),
            0x48 to Operation(1, 4, "LD C,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.B) }),
            0x49 to Operation(1, 4, "LD C,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.C) }),
            0x4A to Operation(1, 4, "LD C,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.D) }),
            0x4B to Operation(1, 4, "LD C,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.E) }),
            0x4C to Operation(1, 4, "LD C,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.H) }),
            0x4D to Operation(1, 4, "LD C,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.L) }),
            0x4E to Operation(1, 8, "LD C,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.C, Registers.Bit16.HL) }),
            0x4F to Operation(1, 4, "LD C,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.C, Registers.Bit8.A) }),

            0x50 to Operation(1, 4, "LD D,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.B) }),
            0x51 to Operation(1, 4, "LD D,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.C) }),
            0x52 to Operation(1, 4, "LD D,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.D) }),
            0x53 to Operation(1, 4, "LD D,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.E) }),
            0x54 to Operation(1, 4, "LD D,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.H) }),
            0x55 to Operation(1, 4, "LD D,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.L) }),
            0x56 to Operation(1, 8, "LD D,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.D, Registers.Bit16.HL) }),
            0x57 to Operation(1, 4, "LD D,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.D, Registers.Bit8.A) }),
            0x58 to Operation(1, 4, "LD E,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.B) }),
            0x59 to Operation(1, 4, "LD E,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.C) }),
            0x5A to Operation(1, 4, "LD E,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.D) }),
            0x5B to Operation(1, 4, "LD E,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.E) }),
            0x5C to Operation(1, 4, "LD E,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.H) }),
            0x5D to Operation(1, 4, "LD E,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.L) }),
            0x5E to Operation(1, 8, "LD E,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.E, Registers.Bit16.HL) }),
            0x5F to Operation(1, 4, "LD E,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.E, Registers.Bit8.A) }),

            0x60 to Operation(1, 4, "LD H,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.B) }),
            0x61 to Operation(1, 4, "LD H,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.C) }),
            0x62 to Operation(1, 4, "LD H,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.D) }),
            0x63 to Operation(1, 4, "LD H,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.E) }),
            0x64 to Operation(1, 4, "LD H,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.H) }),
            0x65 to Operation(1, 4, "LD H,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.L) }),
            0x66 to Operation(1, 8, "LD H,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.H, Registers.Bit16.HL) }),
            0x67 to Operation(1, 4, "LD H,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.H, Registers.Bit8.A) }),
            0x68 to Operation(1, 4, "LD L,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.B) }),
            0x69 to Operation(1, 4, "LD L,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.C) }),
            0x6A to Operation(1, 4, "LD L,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.D) }),
            0x6B to Operation(1, 4, "LD L,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.E) }),
            0x6C to Operation(1, 4, "LD L,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.H) }),
            0x6D to Operation(1, 4, "LD L,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.L) }),
            0x6E to Operation(1, 8, "LD L,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.L, Registers.Bit16.HL) }),
            0x6F to Operation(1, 4, "LD L,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.L, Registers.Bit8.A) }),

            0x70 to Operation(1, 8, "LD (HL),B", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.B) }),
            0x71 to Operation(1, 8, "LD (HL),C", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.C) }),
            0x72 to Operation(1, 8, "LD (HL),D", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.D) }),
            0x73 to Operation(1, 8, "LD (HL),E", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.E) }),
            0x74 to Operation(1, 8, "LD (HL),H", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.H) }),
            0x75 to Operation(1, 8, "LD (HL),L", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.L) }),
            0x76 to Operation(1, 4, "HALT", { cpu -> cpu.halt() }),
            0x77 to Operation(1, 8, "LD (HL),A", { cpu -> cpu.loadRegisterIntoIndirectLocation(Registers.Bit16.HL, Registers.Bit8.A) }),
            0x78 to Operation(1, 4, "LD A,B", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.B) }),
            0x79 to Operation(1, 4, "LD A,C", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.C) }),
            0x7A to Operation(1, 4, "LD A,D", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.D) }),
            0x7B to Operation(1, 4, "LD A,E", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.E) }),
            0x7C to Operation(1, 4, "LD A,H", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.H) }),
            0x7D to Operation(1, 4, "LD A,L", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.L) }),
            0x7E to Operation(1, 8, "LD A,(HL)", { cpu -> cpu.loadIndirectValueIntoRegister(Registers.Bit8.A, Registers.Bit16.HL) }),
            0x7F to Operation(1, 4, "LD A,A", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit8.A, Registers.Bit8.A) }),

            0x80 to Operation(1, 4, "ADD A,B", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.B) }),
            0x81 to Operation(1, 4, "ADD A,C", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.C) }),
            0x82 to Operation(1, 4, "ADD A,D", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.D) }),
            0x83 to Operation(1, 4, "ADD A,E", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.E) }),
            0x84 to Operation(1, 4, "ADD A,H", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.H) }),
            0x85 to Operation(1, 4, "ADD A,L", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.L) }),
            0x86 to Operation(1, 4, "ADD A,(HL)", { cpu -> cpu.add8Indirect(Registers.Bit8.A, Registers.Bit16.HL) }),
            0x87 to Operation(1, 4, "ADD A,A", { cpu -> cpu.add8(Registers.Bit8.A, Registers.Bit8.A) }),
            0x88 to Operation(1, 4, "ADC A,B", { cpu -> cpu.adc(Registers.Bit8.B) }),
            0x89 to Operation(1, 4, "ADC A,C", { cpu -> cpu.adc(Registers.Bit8.C) }),
            0x8A to Operation(1, 4, "ADC A,D", { cpu -> cpu.adc(Registers.Bit8.D) }),
            0x8B to Operation(1, 4, "ADC A,E", { cpu -> cpu.adc(Registers.Bit8.E) }),
            0x8C to Operation(1, 4, "ADC A,H", { cpu -> cpu.adc(Registers.Bit8.H) }),
            0x8D to Operation(1, 4, "ADC A,L", { cpu -> cpu.adc(Registers.Bit8.L) }),
            0x8E to Operation(1, 4, "ADC A,B", { cpu -> cpu.adcIndirect(Registers.Bit16.HL) }),
            0x8F to Operation(1, 4, "ADC A,A", { cpu -> cpu.adc(Registers.Bit8.A) }),

            0x90 to Operation(1, 4, "SUB A,B", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.B) }),
            0x91 to Operation(1, 4, "SUB A,C", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.C) }),
            0x92 to Operation(1, 4, "SUB A,D", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.D) }),
            0x93 to Operation(1, 4, "SUB A,E", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.E) }),
            0x94 to Operation(1, 4, "SUB A,H", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.H) }),
            0x95 to Operation(1, 4, "SUB A,L", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.L) }),
            0x96 to Operation(1, 4, "SUB A,(HL)", { cpu -> cpu.sub8Indirect(Registers.Bit8.A, Registers.Bit16.HL) }),
            0x97 to Operation(1, 4, "SUB A,A", { cpu -> cpu.sub8(Registers.Bit8.A, Registers.Bit8.A) }),
            0x98 to Operation(1, 4, "ADC A,B", { cpu -> cpu.sbc(Registers.Bit8.B) }),
            0x99 to Operation(1, 4, "ADC A,C", { cpu -> cpu.sbc(Registers.Bit8.C) }),
            0x9A to Operation(1, 4, "ADC A,D", { cpu -> cpu.sbc(Registers.Bit8.D) }),
            0x9B to Operation(1, 4, "ADC A,E", { cpu -> cpu.sbc(Registers.Bit8.E) }),
            0x9C to Operation(1, 4, "ADC A,H", { cpu -> cpu.sbc(Registers.Bit8.H) }),
            0x9D to Operation(1, 4, "ADC A,L", { cpu -> cpu.sbc(Registers.Bit8.L) }),
            0x9E to Operation(1, 4, "ADC A,B", { cpu -> cpu.sbcIndirect(Registers.Bit16.HL) }),
            0x9F to Operation(1, 4, "ADC A,A", { cpu -> cpu.sbc(Registers.Bit8.A) }),

            0xA0 to Operation(1, 4, "AND B", { cpu -> cpu.and(Registers.Bit8.B) }),
            0xA1 to Operation(1, 4, "AND C", { cpu -> cpu.and(Registers.Bit8.C) }),
            0xA2 to Operation(1, 4, "AND D", { cpu -> cpu.and(Registers.Bit8.D) }),
            0xA3 to Operation(1, 4, "AND E", { cpu -> cpu.and(Registers.Bit8.E) }),
            0xA4 to Operation(1, 4, "AND H", { cpu -> cpu.and(Registers.Bit8.H) }),
            0xA5 to Operation(1, 4, "AND L", { cpu -> cpu.and(Registers.Bit8.L) }),
            0xA6 to Operation(1, 4, "AND (HL)", { cpu -> cpu.andIndirect(Registers.Bit16.HL) }),
            0xA7 to Operation(1, 4, "AND A", { cpu -> cpu.and(Registers.Bit8.A) }),
            0xA8 to Operation(1, 4, "XOR B", { cpu -> cpu.xor(Registers.Bit8.B) }),
            0xA9 to Operation(1, 4, "XOR C", { cpu -> cpu.xor(Registers.Bit8.C) }),
            0xAA to Operation(1, 4, "XOR D", { cpu -> cpu.xor(Registers.Bit8.D) }),
            0xAB to Operation(1, 4, "XOR E", { cpu -> cpu.xor(Registers.Bit8.E) }),
            0xAC to Operation(1, 4, "XOR H", { cpu -> cpu.xor(Registers.Bit8.H) }),
            0xAD to Operation(1, 4, "XOR L", { cpu -> cpu.xor(Registers.Bit8.L) }),
            0xAE to Operation(1, 4, "XOR (HL)", { cpu -> cpu.xorIndirect(Registers.Bit16.HL) }),
            0xAF to Operation(1, 4, "XOR A", { cpu -> cpu.xor(Registers.Bit8.A) }),

            0xB0 to Operation(1, 4, "OR B", { cpu -> cpu.or(Registers.Bit8.B) }),
            0xB1 to Operation(1, 4, "OR C", { cpu -> cpu.or(Registers.Bit8.C) }),
            0xB2 to Operation(1, 4, "OR D", { cpu -> cpu.or(Registers.Bit8.D) }),
            0xB3 to Operation(1, 4, "OR E", { cpu -> cpu.or(Registers.Bit8.E) }),
            0xB4 to Operation(1, 4, "OR H", { cpu -> cpu.or(Registers.Bit8.H) }),
            0xB5 to Operation(1, 4, "OR L", { cpu -> cpu.or(Registers.Bit8.L) }),
            0xB6 to Operation(1, 4, "OR (HL)", { cpu -> cpu.orIndirect(Registers.Bit16.HL) }),
            0xB7 to Operation(1, 4, "OR A", { cpu -> cpu.or(Registers.Bit8.A) }),
            0xB8 to Operation(1, 4, "CP B", { cpu -> cpu.compare(Registers.Bit8.B) }),
            0xB9 to Operation(1, 4, "CP C", { cpu -> cpu.compare(Registers.Bit8.C) }),
            0xBA to Operation(1, 4, "CP D", { cpu -> cpu.compare(Registers.Bit8.D) }),
            0xBB to Operation(1, 4, "CP E", { cpu -> cpu.compare(Registers.Bit8.E) }),
            0xBC to Operation(1, 4, "CP H", { cpu -> cpu.compare(Registers.Bit8.H) }),
            0xBD to Operation(1, 4, "CP L", { cpu -> cpu.compare(Registers.Bit8.L) }),
            0xBE to Operation(1, 4, "CP (HL)", { cpu -> cpu.compareIndirect(Registers.Bit16.HL) }),
            0xBF to Operation(1, 4, "CP A", { cpu -> cpu.compare(Registers.Bit8.A) }),

            0xC0 to Operation(1, 20, "RET NZ", { cpu -> cpu.retFlag(Cpu.Flag.Z, false) }, true, 8),
            0xC1 to Operation(1, 12, "POP BC", { cpu -> cpu.popInto(Registers.Bit16.BC) }),
            0xC2 to Operation(3, 16, "JP NZ,a16", { cpu -> cpu.jumpImmediateFlag(Cpu.Flag.Z, false) }, true, 12),
            0xC3 to Operation(3, 16, "JP a16", { cpu -> cpu.jumpImmediate() }, true, 16),
            0xC4 to Operation(3, 24, "CALL NZ,a16", { cpu -> cpu.callImmediateFlag(Cpu.Flag.Z, false) }, true, 12),
            0xC5 to Operation(1, 16, "PUSH BC", { cpu -> cpu.pushRegister(Registers.Bit16.BC) }),
            0xC6 to Operation(2, 8, "ADD A,d8", { cpu -> cpu.add8Immediate() }),
            0xC7 to Operation(1, 16, "RST 00H", { cpu -> cpu.reset(0x00) }),
            0xC8 to Operation(1, 20, "RET Z", { cpu -> cpu.retFlag(Cpu.Flag.Z, true) }, true, 8),
            0xC9 to Operation(1, 16, "RET", { cpu -> cpu.ret() }, true, 16),
            0xCA to Operation(3, 16, "JP Z,a16", { cpu -> cpu.jumpImmediateFlag(Cpu.Flag.Z, true) }, true, 12),
            // CPU Handles this by checking the PC value, so this is never actually used
            0xCB to Operation(1, 4, "PREFIX CB", { cpu -> throw RuntimeException("Should not execute CB directly!") }),
            0xCC to Operation(3, 24, "CALL Z,a16", { cpu -> cpu.callImmediateFlag(Cpu.Flag.Z, true) }, true, 12),
            0xCD to Operation(3, 24, "CALL a16", { cpu -> cpu.callImmediate() }, true, 24),
            0xCE to Operation(2, 8, "ADC A,d8", { cpu -> cpu.adcImmediate() }),
            0xCF to Operation(1, 16, "RST 08H", { cpu -> cpu.reset(0x08) }, true, 16),

            0xD0 to Operation(1, 20, "RET NC", { cpu -> cpu.retFlag(Cpu.Flag.C, false) }, true, 8),
            0xD1 to Operation(1, 12, "POP DE", { cpu -> cpu.popInto(Registers.Bit16.DE) }),
            0xD2 to Operation(3, 16, "JP NC,a16", { cpu -> cpu.jumpImmediateFlag(Cpu.Flag.C, false) }, true, 12),
            0xD3 to invalid,
            0xD4 to Operation(3, 24, "CALL NC,a16", { cpu -> cpu.callImmediateFlag(Cpu.Flag.C, false) }, true, 12),
            0xD5 to Operation(1, 16, "PUSH DE", { cpu -> cpu.pushRegister(Registers.Bit16.DE) }),
            0xD6 to Operation(2, 8, "SUB d8", { cpu -> cpu.sub8Immediate() }),
            0xD7 to Operation(1, 16, "RST 10H", { cpu -> cpu.reset(0x10) }, true, 16),
            0xD8 to Operation(1, 20, "RET C", { cpu -> cpu.retFlag(Cpu.Flag.C, true) }, true, 8),
            0xD9 to Operation(1, 16, "RETI", { cpu -> cpu.ime = true; cpu.ret() }, true, 16),
            0xDA to Operation(3, 16, "JP C,a16", { cpu -> cpu.jumpImmediateFlag(Cpu.Flag.C, true) }, true, 12),
            0xDB to invalid,
            0xDC to Operation(3, 24, "CALL C,a16", { cpu -> cpu.callImmediateFlag(Cpu.Flag.C, true) }, true, 12),
            0xDD to invalid,
            0xDE to Operation(2, 8, "SBC A,d8", { cpu -> cpu.sbcImmediate() }),
            0xDF to Operation(1, 16, "RST 18H", { cpu -> cpu.reset(0x18) }, true, 16),

            0xE0 to Operation(2, 12, "LDH (a8),A", { cpu -> cpu.ldhRegisterValueIntoImmediate() }),
            0xE1 to Operation(1, 12, "POP HL", { cpu -> cpu.popInto(Registers.Bit16.HL) }),
            0xE2 to Operation(1, 8, "LD (C),A", { cpu -> cpu.loadRegisterAValueIntoIndirectRegisterLocation(Registers.Bit8.C) }),
            0xE3 to invalid,
            0xE4 to invalid,
            0xE5 to Operation(1, 16, "PUSH HL", { cpu -> cpu.pushRegister(Registers.Bit16.HL) }),
            0xE6 to Operation(2, 8, "AND d8", { cpu -> cpu.andImmediate() }),
            0xE7 to Operation(1, 16, "RST 20H", { cpu -> cpu.reset(0x20) }, true, 16),
            0xE8 to Operation(2, 16, "ADD SP,r8", { cpu -> cpu.add8ImmediateToSp() }),
            0xE9 to Operation(1, 4, "JP (HL)", { cpu -> cpu.jumpRegister(Registers.Bit16.HL) }),
            0xEA to Operation(3, 16, "LD (a16),A", { cpu -> cpu.loadRegisterIntoImmediateLocation(Registers.Bit8.A) }),
            0xEB to invalid,
            0xEC to invalid,
            0xED to invalid,
            0xEE to Operation(2, 8, "XOR d8", { cpu -> cpu.xor8Immediate() }),
            0xEF to Operation(1, 16, "RST 28H", { cpu -> cpu.reset(0x28) }, true, 16),

            0xF0 to Operation(2, 12, "LDH A,(a8)", { cpu -> cpu.ldhImmediateMemoryLocationIntoRegister() }),
            0xF1 to Operation(1, 12, "POP HL", { cpu -> cpu.popInto(Registers.Bit16.HL) }),
            0xF2 to Operation(1, 8, "LD A,(C)", { cpu -> cpu.loadIndirectRegisterValueIntoRegisterA(Registers.Bit8.C) }),
            0xF3 to Operation(1, 4, "DI", { cpu -> cpu.ime = false }),
            0xF4 to invalid,
            0xF5 to Operation(1, 16, "PUSH AF", { cpu -> cpu.pushRegister(Registers.Bit16.AF) }),
            0xF6 to Operation(2, 8, "OR d8", { cpu -> cpu.or8Immediate() }),
            0xF7 to Operation(1, 16, "RST 30H", { cpu -> cpu.reset(0x30) }, true, 16),
            0xF8 to Operation(2, 12, "LD HL,SP+r8", { cpu -> cpu.loadRegisterWithImmediateOffsetIntoRegister(Registers.Bit16.HL, Registers.Bit16.SP) }),
            0xF9 to Operation(1, 8, "LD SP,HL", { cpu -> cpu.loadRegisterIntoRegister(Registers.Bit16.SP, Registers.Bit16.HL) }),
            0xFA to Operation(3, 16, "LD A,(a16)", { cpu -> cpu.loadImmediateLocationIntoRegister(Registers.Bit8.A) }),
            0xFB to Operation(1, 4, "EI", { cpu -> cpu.ime = true }),
            0xFC to invalid,
            0xFD to invalid,
            0xFE to Operation(2, 8, "CP d8", { cpu -> cpu.compareImmediate() }),
            0xFF to Operation(1, 16, "RST 38H", { cpu -> cpu.reset(0x38) }, true, 16)
    )
}