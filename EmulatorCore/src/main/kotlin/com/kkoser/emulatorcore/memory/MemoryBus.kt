package com.kkoser.emulatorcore.memory

import java.util.logging.Level
import java.util.logging.Logger

/*
Memory sections:
    0000-3FFF 16KB ROM Bank 00 (in cartridge, fixed at bank 00)
    4000-7FFF 16KB ROM Bank 01..NN (in cartridge, switchable bank number)
    8000-9FFF 8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
    A000-BFFF 8KB External RAM (in cartridge, switchable bank, if any)
    C000-CFFF 4KB Work RAM Bank 0 (WRAM)
    D000-DFFF 4KB Work RAM Bank 1 (WRAM) (switchable bank 1-7 in CGB Mode)
    E000-FDFF Same as C000-DDFF (ECHO) (typically not used)
    FE00-FE9F Sprite Attribute Table (OAM)
    FEA0-FEFF Not Usable
    FF00-FF7F I/O Ports
    FF80-FFFE High RAM (HRAM)
    FFFF Interrupt Enable Register
 */
class MemoryBus(cartridgeMemory: CartridgeMemory) {
    private val cartridgeMemory = cartridgeMemory
    private val internalRam = Array(8192, {0})
    private val vram = Array(8192, {0})
    // OAM memory holds 40 sprites at 4 bytes each, so 160 bytes
    private val oamRam = Array(160, {0})
    private val hram = Array(127, {0})
    fun read(position: Int): Int {
        // Things to implement here:
        /*
        - wrap first 32k into rom via a rom class (for mbc support)
        - add checks for vram availability based on vlock flags (can't be read outside of vblank)
        - map external ram also to ROM file
        - mirror empty part of ram to normal ram?
         */
        when(position) {
            in 0..0x8000 -> {
                return cartridgeMemory.read(position)
            }
            in 0x8000..0x9FFF -> {
                // VRAM is only accessible during v/hlock
                // TODO: add vlock checks
            }
            in 0xA000..0xBFFF -> {
                // external RAM
                return cartridgeMemory.read(position)
            }
            in 0xC000..0xCFFF -> {
                // Working ram bank 1
                return internalRam[position - 0xC000]
            }
            in 0xD000..0xDFFF -> {
                // Work ram bank 2 (only switchable in GBC)
                return internalRam[position - 0xC000]
            }
            in  0xE000..0xFDFF -> {
                // Echo memory
            }
            in 0xFE00..0xFE9F -> {
                // OAM
                return oamRam[position - 0xFE00]
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
                return 0
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
            }
            in 0xFF80..0xFFFE -> {
                return hram[position - 0xFF80]
                // HRAM
            }
            0xFFFF -> {
                throw RuntimeException("The cpu wasn't able to intercept the read to the IME")
                // IME
            }
        }
        return 0
    }

    fun readSigned(position: Int): Int {
        when(position) {
            in 0..0x8000 -> {
                return cartridgeMemory.readSigned(position)
            }
            in 0x8000..0x9FFF -> {
                // VRAM is only accessible during v/hlock
                // TODO: add vlock checks
            }
            in 0xA000..0xBFFF -> {
                // external RAM
                return cartridgeMemory.readSigned(position)
            }
            in 0xC000..0xCFFF -> {
                // Working ram bank 1
                return internalRam[position - 0xC000]
            }
            in 0xD000..0xDFFF -> {
                // Work ram bank 2 (only switchable in GBC)
                return internalRam[position - 0xC000]
            }
            in  0xE000..0xFDFF -> {
                // Echo memory
            }
            in 0xFE00..0xFE9F -> {
                // OAM
                return oamRam[position - 0xFE00]
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
                return 0
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
            }
            in 0xFF80..0xFFFE -> {
                return hram[position - 0xFF80]
                // HRAM
            }
            0xFFFF -> {
                throw RuntimeException("The cpu wasn't able to intercept the read to the IME")
                // IME
            }
        }
        return 0
    }

    fun write(position: Int, value: Int) {
        when(position) {
            in 0..0x8000 -> {
                cartridgeMemory.write(position, value)
            }
            in 0x8000..0x9FFF -> {
                // VRAM is only writable during v/hlock
                // TODO: add vlock checks
            }
            in 0xA000..0xBFFF -> {
                // external RAM
                cartridgeMemory.write(position, value)
            }
            in 0xC000..0xCFFF -> {
                // Working ram bank 1
                internalRam[position - 0xC000] = value
            }
            in 0xD000..0xDFFF -> {
                // Work ram bank 2 (only switchable in GBC)
                internalRam[position - 0xC000] = value
            }
            in  0xE000..0xFDFF -> {
                // Echo memory
            }
            in 0xFE00..0xFE9F -> {
                // OAM
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
            }
            in 0xFF80..0xFFFE -> {
                // HRAM
                hram[position - 0xFF80] = value
            }
            0xFFFF -> {
                throw RuntimeException("The cpu wasn't able to intercept the write to the IME")
                // IME
            }
        }
    }
}