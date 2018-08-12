package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.toUnsigned8BitInt

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
class MemoryBus(cartridgeMemory: CartridgeMemory, timer: Timer, interruptHandler: InterruptHandler, lcd: Lcd, val dma: Dma, val gpu: Gpu) {
    private val cartridgeMemory = cartridgeMemory
    private val timer = timer
    private val interruptHandler = interruptHandler
    private val lcd = lcd
    private val internalRam = Array(8192, {0})
    private val vram = Array(8192, {0})
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
                // This check is handled internally by the gpu
                return gpu.read(position)
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
                return gpu.read(position)
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
                return 0
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
                when(position) {
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
                        throw RuntimeException("trying to read timer frequency, you should go implement that")
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

    fun readSigned(position: Int): Int {
        when(position) {
            in 0..0x8000 -> {
                return cartridgeMemory.readSigned(position)
            }
            in 0x8000..0x9FFF -> {
                // VRAM is only accessible during v/hlock
                // This check is handled internally by the gpu
                return gpu.read(position)
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
                return gpu.read(position)
            }
            in 0xFEA0..0xFEFF -> {
                // not usabled
                return 0
            }
            in 0xFF00..0xFF7F -> {
                // IO ports
                when(position) {
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
                        throw RuntimeException("trying to read timer frequency, you should go implement that")
                    }
                    0xFF0F -> {
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
                // Echo memory
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
                    0xFF01 -> {
                        // TODO: Implement serial
                        // no-op, serial
                    }
                    0xFF02 -> {
                        // TODO: Implement serial
                        // no-op, serial
                    }
                    0xFF04 -> {
                        // Divider register
                        timer.resetDivider()
                    }
                    0xFF05 -> {
                        // Timer value
                        throw RuntimeException("Trying to write the timer value")
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
                        dma.write(value, this)
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