package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.TestInterruptHandler
import com.kkoser.emulatorcore.cpu.InterruptHandler
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class LcdShould {
    var target: Lcd? = null
    val interruptHandler = TestInterruptHandler()

    @Before
    fun setup() {
        target = Lcd()
        target?.control = 0b11111111
        interruptHandler.setup()
    }

    @Test
    fun updateActiveLineDuringVBlank() {
        target?.mode = Lcd.Mode.V_BLANK
        target?.currentScanLine = Lcd.V_BLANK_START_LINE

        for (i in (Lcd.V_BLANK_START_LINE + 1)..Lcd.LINE_COUNT) {
            cycleLcd(Lcd.Mode.V_BLANK.lengthInCycles)
            assertEquals(target?.currentScanLine, i)
        }
    }

    @Test
    fun triggerVBlankInterruptWhenMovingIntoMode() {
        target?.mode = Lcd.Mode.H_BLANK
        target?.currentScanLine = Lcd.V_BLANK_START_LINE - 1

        cycleLcd(Lcd.Mode.H_BLANK.lengthInCycles + 1)

        assertEquals(target?.mode, Lcd.Mode.V_BLANK)
        assertEquals(target?.currentScanLine, Lcd.V_BLANK_START_LINE)
        assertEquals(interruptHandler.interrupts.size, 1)
        assertEquals(interruptHandler.interrupts[0], InterruptHandler.Interrupt.V_BLANK)

    }

    @Test
    fun goThroughModesInOrder() {
        target?.mode = Lcd.Mode.OAM_SEARCH
        target?.currentScanLine = 0

        cycleLcd(Lcd.Mode.OAM_SEARCH.lengthInCycles)
        assertEquals(target?.mode, Lcd.Mode.LCD_TRANSFER)

        cycleLcd(Lcd.Mode.LCD_TRANSFER.lengthInCycles)
        assertEquals(target?.mode, Lcd.Mode.H_BLANK)

        cycleLcd(Lcd.Mode.H_BLANK.lengthInCycles)
        assertEquals(target?.mode, Lcd.Mode.OAM_SEARCH)
        assertEquals(target?.currentScanLine, 1)
    }

    @Test
    fun moveFromVBlankToFirstLine() {
        target?.mode = Lcd.Mode.V_BLANK
        target?.currentScanLine = Lcd.LINE_COUNT

        cycleLcd(Lcd.Mode.V_BLANK.lengthInCycles)
        assertEquals(target?.mode, Lcd.Mode.OAM_SEARCH)
        assertEquals(target?.currentScanLine, 0)
    }



    // Just ticks repeatedly by 12 cycles until the requested total number of cycles has been processed
    // It's important to do this in increments to correctly progress through modes
    // This function allows us to cycles through large amounts of cycles (on the order of hundreds
    // or thousands) and still progress through the right modes
    private fun cycleLcd(cycles: Int) {
        var remainingCycles = cycles
        while(remainingCycles > 0) {
            target?.tick(12, interruptHandler)
            remainingCycles -= 12
        }
    }
}