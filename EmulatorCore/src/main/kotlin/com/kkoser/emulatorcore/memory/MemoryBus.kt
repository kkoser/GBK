package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.cpu.LDH_OFFSET
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toUnsigned8BitInt
import com.kkoser.emulatorcore.cpu.forceLineTo0
import com.kkoser.emulatorcore.io.Joypad
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
class MemoryBus(cartridgeMemory: CartridgeMemory, timer: Timer, interruptHandler: InterruptHandler, lcd: Lcd, val dma: Dma, val gpu: Gpu, private val joyPad: Joypad) {
    private val cartridgeMemory = cartridgeMemory
    private val timer = timer
    private val interruptHandler = interruptHandler
    private val lcd = lcd
    private val internalRam = Array(8192, {0})
    private val hram = Array(127, {0})
    private var bootRomEnabled = true
    private var hackyShitLastChar: String = ""
    fun read(position: Int): Int {
//        if (position > LDH_OFFSET)
//            println("reading at position ${position.toHexString()}")
//         Things to implement here:
        /*
        - wrap first 32k into rom via a rom class (for mbc support)
        - add checks for vram availability based on vlock flags (can't be read outside of vblank)
        - map external ram also to ROM file
        - mirror empty part of ram to normal ram?
         */
        // Cannot access anythin but hram when a dma transfer is haappening
        if (dma.isWriting && position < 0xFF80) {
            return 0xFF
        }
        when(position) {
            in 0 until 0x8000 -> {
                if (bootRomEnabled && position < 0x100) {
                    return gameboyClassicBootRom[position]
                }
                return cartridgeMemory.read(position)
            }
            in 0x8000 until 0x9FFF -> {
                // VRAM is only accessible during v/hlock
                // This check is handled internally by the gpu
                return gpu.read(position)
            }
            in 0xA000 until 0xBFFF -> {
                // external RAM
                return cartridgeMemory.read(position)
            }
            in 0xC000 until 0xCFFF -> {
                // Working ram bank 1
                return internalRam[position - 0xC000]
            }
            in 0xD000 until 0xDFFF -> {
                // Work ram bank 2 (only switchable in GBC)
                return internalRam[position - 0xC000]
            }
            in  0xE000 until 0xFDFF -> {
                return read(position - (0xE000-0xC000))
            }
            in 0xFE00 until 0xFE9F -> {
                // OAM
                return gpu.read(position)
            }
            in 0xFEA0 until 0xFEFF -> {
                // not usabled
                return 0
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
                when(position) {
                    0xFF00 -> {
                        // Joypad
                        return joyPad.memoryValue
                    }
                    0xFF04 -> {
                        return timer.dividerCount
                    }
                    0xFF05 -> {
                        // Timer value
                        return timer.count
                    }
                    0xFF06 -> {
                        // Timer modulator
                        return timer.offset
                    }
                    0xFF07 -> {
                        // Timer frequency
                        return timer.getTmc()
                    }
                    0xFF0F -> {
                        // IF register for pending interrupts
                        return interruptHandler.registerIF
                    }

                    // Sound register, currently always off
                    0xFF24 -> {
                        return 0
                    }
                    0xFF25 -> {
                        return 0
                    }
                    0xFF26 -> {
                        return 0
                    }

                    // region LCD
                    0xFF40 -> {
                        return lcd.control
                    }
                    0xFF41 -> {
                        return lcd.status
                    }
                    0xFF42 -> {
                        return lcd.scrollY
                    }
                    0xFF43 -> {
                        return lcd.scrollX
                    }
                    0xFF44 -> {
                        if (forceLineTo0)
                            return 0
                        return lcd.currentScanLine
                    }
                    0xFF45 -> {
                        return lcd.lineCompare
                    }

                    // region GPU
                    0xFF47 -> {
                        return gpu.bgPallete
                    }
                    0xFF48 -> {
                        return gpu.obj0Pallete
                    }
                    0xFF49 -> {
                        return gpu.obj1Pallete
                    }
                }
            }
            in 0xFF80..0xFFFE -> {
                return hram[position - 0xFF80]
                // HRAM
            }
            0xFFFF -> {
                return interruptHandler.registerIE
            }
        }
        return 0
    }

    fun write(position: Int, value: Int) {

        if (dma.isWriting && position < 0xFF80) {
            return
        }

        when(position) {
            in 0..0x8000 -> {
                cartridgeMemory.write(position, value)
            }
            in 0x8000..0x9FFF -> {
                // VRAM is only writable during v/hlock
                // This check is handled internally by the gpu
                return gpu.write(position, value)
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
                return write(position - (0xE000-0xC000), value)
            }
            in 0xFE00..0xFE9F -> {
                gpu.write(position, value)
                // OAM
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
                when(position) {
                    0xFF00 -> {
                        // joypad

                    }
                    0xFF01 -> {
                        if (hackyShitLastChar.endsWith("ok")) {
                            // Sttart the breakpoint
                            hackyShitLastChar = ""
                        }
                        if (hackyShitLastChar.endsWith("04:o") && "${value.toChar()}" == "k") {
                            print("teeset 11 just finished\n")
//                            Cpu.Companion.loggingEnabled = true
                        }
                        hackyShitLastChar += "${value.toChar()}"
//                        print(hackyShitLastChar)

//                        if (hackyShitLastChar.endsWith("05:ok") && Cpu.Companion.loggingEnabled) {
//                            throw RuntimeException("farts")
//                        }


//                         TODO: Implement serial
                        // no-op, serial
//                        TODO("Implement serial")
                    }
                    0xFF02 -> {
//                        println("FF02: ${value.toChar()}")
                        // TODO: Implement serial
                        // no-op, serial
//                        TODO("Implement serial")
                    }
                    0xFF04 -> {
                        // Divider register
                        timer.resetDivider()
                    }
                    0xFF05 -> {
                        // Timer value
                        timer.count = value.toUnsigned8BitInt()
                    }
                    0xFF06 -> {
                        // Timer modulator
                        timer.offset = value
                    }
                    0xFF07 -> {
                        // Timer frequency
                        timer.setTMC(value)
                    }
                    0xFF0F -> {
                        interruptHandler.registerIF = value.toUnsigned8BitInt()
                    }

                    // Sound registers, currently always off
                    0xFF24 -> {
                        // no-op, no sound
                    }
                    0xFF25 -> {
                        // no-op, no sound
                    }
                    0xFF26 -> {
                       // no-op, no sound
                    }

                    // region LCD
                    0xFF40 -> {
                        lcd.control = value
                    }
                    0xFF41 -> {
                        lcd.status = value
                    }
                    0xFF42 -> {
                        lcd.scrollY = value
                    }
                    0xFF43 -> {
                        lcd.scrollX = value
                    }
                    0xFF44 -> {
                        lcd.resetScanLine();
                    }
                    0xFF45 -> {
                        lcd.lineCompare = value
                    }
                    0xFF46 -> {
                        dma.write(value)
                    }

                    // region GPU
                    0xFF47 -> {
                        gpu.bgPallete = value
                    }
                    0xFF48 -> {
                        gpu.obj0Pallete = value
                    }
                    0xFF49 -> {
                        gpu.obj1Pallete = value
                    }
                    0xFF50 -> {
                        bootRomEnabled = false
                    }

                    else -> {
                        // unused hardware io locations?
//                        throw RuntimeException("unsupported write address ${Integer.toHexString(position)}")
                    }
                }
            }
            in 0xFF80..0xFFFE -> {
                // HRAM
                hram[position - 0xFF80] = value
            }
            0xFFFF -> {
                // IE
                interruptHandler.registerIE = value.toUnsigned8BitInt()
            }
        }
    }
}

private val gameboyClassicBootRom = intArrayOf(
        0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF, 0x0E,
        0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0,
        0x47, 0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B,
        0xFE, 0x34, 0x20, 0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9,
        0x3E, 0x19, 0xEA, 0x10, 0x99, 0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20,
        0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E, 0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04,
        0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20, 0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2,
        0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E, 0xC1, 0xFE, 0x64, 0x20, 0x06,
        0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xE2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20, 0xD2, 0x05, 0x20,
        0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11, 0x17,
        0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B,
        0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E,
        0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC,
        0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E, 0x3C, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x3C,
        0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A, 0x13, 0xBE, 0x00, 0x00, 0x23, 0x7D, 0xFE, 0x34, 0x20,
        0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB, 0x86, 0x00, 0x00, 0x3E, 0x01, 0xE0, 0x50
)