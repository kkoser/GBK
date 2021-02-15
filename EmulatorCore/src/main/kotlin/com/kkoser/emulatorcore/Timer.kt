package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.cpu.InterruptHandler

/**
 * A class to manage the timer registers and divider register
 */
class Timer {
    companion object {
        const val CPU_SPEED = 4194304
        const val CYCLES_PER_DIVIDER_CHANGE = 256
        const val DIVIDER_MAX = 255
    }

    // Timer fields
    var enabled = true
    private var currentIncreaseLimitCycles =  4096
    private var cyclesUntilIncrement = 0
    var count = 0
    private var currentMode = 0
    var offset = 0
        set(value) { field = value % 255 }

    // Divider fields
    private var dividerCycleCounter = 0
    var dividerCount = 0
        private set

    fun tick(cyclesThisTick: Int, interruptHandler: InterruptHandler) {
        // Do the divider first, since it cannot be turned off
        dividerCycleCounter += cyclesThisTick
        if (dividerCycleCounter > CYCLES_PER_DIVIDER_CHANGE) {
            dividerCycleCounter -= CYCLES_PER_DIVIDER_CHANGE
            dividerCount = (dividerCount + 1) % DIVIDER_MAX
        }

        // now that dividers are done, check the enabled flah
        if (!enabled) {
            return
        }

        cyclesUntilIncrement += cyclesThisTick
        if (cyclesUntilIncrement >= currentIncreaseLimitCycles) {
            cyclesUntilIncrement -= currentIncreaseLimitCycles
            count += 1
            if (count > 255) {
                // throw interrupt
                count = offset
                interruptHandler.interrupt(InterruptHandler.Interrupt.CLOCK)
            }
        }

    }

    fun setTMC(memoryValue: Int) {
        when(memoryValue and 0b11) {
            0b00 -> {
                currentIncreaseLimitCycles =  1024 / 4
            }
            0b01 -> {
                currentIncreaseLimitCycles = 16 / 4
            }
            0b10 -> {
                currentIncreaseLimitCycles =  64 / 4
            }
            0b11 -> {
                currentIncreaseLimitCycles = 256 / 4
            }
        }

        currentMode = (memoryValue and 0b11)

        // The high bit is setting it enabled or disabled
        enabled = (memoryValue and 0b100) == 0b100
    }

    // TODO: Make this a property
    fun getTmc(): Int {
        val enabledVal = (if (enabled) 1 else 0) shl 2
        return enabledVal or currentMode
    }

    fun resetDivider() {
        dividerCount = 0
    }
}