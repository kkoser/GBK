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

    var enabled = true
    private var currentMax: Int = CPU_SPEED / 4096
    private var dividerCycleCounter = 0
    var count = 0
        private set
    var dividerCount = 0
        private set
    var offset = 0
        set(value) { field = value % currentMax }

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

        count += cyclesThisTick
        if (count >= currentMax) {
            // throw interrupt
            count = offset
            interruptHandler.interrupt(InterruptHandler.Interrupt.CLOCK)
        }


    }

    fun setTMC(memoryValue: Int) {
        when(memoryValue and 0b11) {
            0b00 -> {
                currentMax = CPU_SPEED / 4096
            }
            0b01 -> {
                currentMax = CPU_SPEED / 262144
            }
            0b10 -> {
                currentMax = CPU_SPEED / 65536
            }
            0b11 -> {
                currentMax = CPU_SPEED / 16384
            }
        }

        // The high bit is setting it enabled or disabled
        enabled = (memoryValue and 0b100) == 0b100
    }

    fun resetDivider() {
        dividerCount = 0
    }
}