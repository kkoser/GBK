package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.cpu.InterruptHandler
import org.jetbrains.annotations.TestOnly

class Lcd {
    companion object {
        val LINE_COUNT = 154
        val V_BLANK_START_LINE = 144
        val SINGLE_LINE_CYCLES = 456
    }

    /**
     * A full line takes 456 cycles, composed of OAM_SEARCH, LCD_TRANSFER, and H_BLANK.
     * V_BLANK is just 10 empty lines, so it is 456 cycles in length and done 10 times
     */
    enum class Mode(val value: Int, val lengthInCycles: Int, val interruptControlBit: Int?) {
        H_BLANK(0b00, 204, 3),
        V_BLANK(0b01, 456, 4),
        OAM_SEARCH(0b10, 80, 5),
        LCD_TRANSFER(0b11, 172, null)
    }

    /**
     * Bit 7: LCD Display Enable             (0=Off, 1=On)
        Bit 6: Window Tile Map Display Select (0=0x9800-0x9BFF, 1=0x9C00-0x9FFF)
        Bit 5: Window Display Enable          (0=Off, 1=On)
        Bit 4: BG & Window Tile Data Select   (0=0x8800-0x97FF, 1=0x8000-0x8FFF)
        Bit 3: BG Tile Map Display Select     (0=0x9800-0x9BFF, 1=0x9C00-0x9FFF)
        Bit 2: OBJ (Sprite) Size              (0=8x8, 1=8x16)
        Bit 1: OBJ (Sprite) Display Enable    (0=Off, 1=On)
        Bit 0: BG Display                     (0=Off, 1=On)
     */
    var control = 0 // display defaults to on?

    /** This is the big one - the LCD status regsiter. It's lower bits are read only,
     * but the higher ones are r/w so the game can enable specific lcd interrupts
     *
     * The bits for this are as follows:
     * Bit 6: LYC=LY Coincidence Interrupt (1=Enable) (Read/Write)
        Bit 5: Mode 2 OAM Interrupt         (1=Enable) (Read/Write)
        Bit 4: Mode 1 V-Blank Interrupt     (1=Enable) (Read/Write)
        Bit 3: Mode 0 H-Blank Interrupt     (1=Enable) (Read/Write)
        Bit 2: Coincidence Flag  (0:LYC<>LY, 1:LYC=LY) (Read Only)
        Bit 1-0: Mode Flag       (Mode 0-3)            (Read Only)
     */
    var status:Int = 0 // This is what it is usually set to with the nintendo boot rom, which we skip

    // scrollX and Y range from  to 255 (0xFF)
    var scrollX = 0
        set(value) {
            field = value % 0xFF
        }
    var scrollY = 0
        set(value) {
            field = value % 0xFF
        }

    var lineCompare = 0

    var currentScanLine = 2
        @TestOnly set
    var mode: Mode = Mode.OAM_SEARCH
        @TestOnly set

    var modeListener: ((Mode) -> Unit)? = null

    private var currentModeCycles = 0

    fun tick(cyclesTaken: Int, interruptHandler: InterruptHandler) {
        if (!enabled()) {
            return
        }

        // Now update the mode
        currentModeCycles += cyclesTaken
        if (currentModeCycles >= mode.lengthInCycles) {
            currentModeCycles -= mode.lengthInCycles
            when(mode) {
                Mode.OAM_SEARCH -> {
                    setMode(Mode.LCD_TRANSFER, interruptHandler)
                }
                Mode.LCD_TRANSFER -> {
                    setMode(Mode.H_BLANK, interruptHandler)
                }
                Mode.H_BLANK -> {
                    if (currentScanLine == V_BLANK_START_LINE - 1) {
                        setMode(Mode.V_BLANK, interruptHandler)
                        setCurrentLine(currentScanLine + 1, interruptHandler)
                        // This timing is off - the interrupts test from blarg (02-interrupt) fails if this is
                        // on, due to the VBLANK interrupt happening and then IME never getting turned back on
                        // with RETI or similar
                        interruptHandler.interrupt(InterruptHandler.Interrupt.V_BLANK)
                    } else {
                        setMode(Mode.OAM_SEARCH, interruptHandler)
                        setCurrentLine(currentScanLine + 1, interruptHandler)
                    }
                }
                Mode.V_BLANK -> {
                    // We stay in V_BLANK for mutliple lines, so check if we're moving to a new line of V_BLANK
                    // or starting to actually draw
                    // TODO: Do we need to correctly increment current line during v-blank?
                    // YES WE DO - IMPLEMENT THIS
                    if (currentScanLine < LINE_COUNT) {
                        setCurrentLine(currentScanLine + 1, interruptHandler)
                    } else {
                        setCurrentLine(0, interruptHandler)
                        setMode(Mode.OAM_SEARCH, interruptHandler)
//                        // TODO: This is not perfectly accurate, we're potentially dropping a couple cycles that could  cause the next vblank to be longer...
//                        currentLineCycles = 0
                    }
                }
            }
        }
    }

    fun setCurrentLine(newLine: Int, interruptHandler: InterruptHandler) {
        // set the coincidence flag in the status register
        if (newLine == lineCompare) {
            status = status or 0b100
            if (status.checkBit(6)) {
                // The coincidence interrupt is enabled, so fire it
                interruptHandler.interrupt(InterruptHandler.Interrupt.LCD)
            }
        } else {
            status = status and 0b11111011
        }

        currentScanLine = newLine
    }

    fun setMode(newMode: Mode, interruptHandler: InterruptHandler) {
//        System.out.println("Setting mode ${mode.value}")
        // clear the bottom 2 bits on status
        status = status and (0b1111100 or newMode.value)

        if (newMode.interruptControlBit != null && status.checkBit(newMode.interruptControlBit)) {
            interruptHandler.interrupt(InterruptHandler.Interrupt.LCD)
        }

        mode = newMode
        modeListener?.invoke(newMode)
    }

    // The game cannot request to go to any scanline - writing any memory to 0xFF44 sets it to 0
    fun resetScanLine() {
        currentScanLine = 0
    }

    fun enabled(): Boolean {
        return control.checkBit(7)
    }
}