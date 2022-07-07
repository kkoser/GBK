package com.kkoser.emulatorcore.io

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.memory.MemoryBus
import com.kkoser.emulatorcore.setBit

// TODO: Joypad interrupt? may not be needed for most games
class Joypad(val interruptHandler: InterruptHandler) {
    companion object {
        private const val MEMORY_LOCATION = 0xFF00
    }

    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    // Either buttons or directions are selected by the game to check
    private var buttonSelected = true

    private var aButtonDepressed = false
    private var bButtonDepressed = false
    private var startButtonDepressed = false
    private var selectButtonDepressed = false

    private var downDepressed = false
    private var upDepressed = false
    private var leftDepressed = false
    private var rightDepressed = false

    var memoryValue: Int
        get() {
            return constructValue()
        }
        set(value) {
            val selectButtons = !value.checkBit(5)
            val selectDirection = !value.checkBit(4)

            if (selectButtons) {
                // They want button info
                buttonSelected = true
            } else if (selectDirection) {
                buttonSelected = false
            } else {
                // They chose neither, default to butons
                buttonSelected = true
            }
        }

    fun aPressed(released: Boolean = false) {
        aButtonDepressed = !released
    }

    fun bPressed(released: Boolean = false) {
        bButtonDepressed = !released
    }

    fun startPressed(released: Boolean = false) {
        startButtonDepressed = !released
    }

    fun selectPressed(released: Boolean = false) {
        selectButtonDepressed = !released
    }

    fun dpadPressed(direction: Direction, released: Boolean = false) {
        when(direction) {
            Direction.UP -> upDepressed = !released
            Direction.DOWN -> downDepressed = !released
            Direction.LEFT -> leftDepressed = !released
            Direction.RIGHT -> rightDepressed = !released
        }
    }

    /*
     *  Bit 7 - Not used
        Bit 6 - Not used
        Bit 5 - P15 Select Button Keys      (0=Select)
        Bit 4 - P14 Select Direction Keys   (0=Select)
        Bit 3 - P13 Input Down  or Start    (0=Pressed) (Read Only)
        Bit 2 - P12 Input Up    or Select   (0=Pressed) (Read Only)
        Bit 1 - P11 Input Left  or Button B (0=Pressed) (Read Only)
        Bit 0 - P10 Input Right or Button A (0=Pressed) (Read Only)
     */
    private fun constructValue(): Int {
        val topBitMask = 0b11000000
        if (buttonSelected) {
            // Directions
            var buttonMask = 0b1111
            buttonMask = buttonMask.setBit(3, startButtonDepressed)
            buttonMask = buttonMask.setBit(2, selectButtonDepressed)
            buttonMask = buttonMask.setBit(1, bButtonDepressed)
            buttonMask = buttonMask.setBit(0, aButtonDepressed)

            return topBitMask or 0b100000 or buttonMask
        } else {
            // Directions
            var buttonMask = 0b1111
            buttonMask = buttonMask.setBit(3, downDepressed)
            buttonMask = buttonMask.setBit(2, upDepressed)
            buttonMask = buttonMask.setBit(1, leftDepressed)
            buttonMask = buttonMask.setBit(0, rightDepressed)

            return topBitMask or 0b010000 or buttonMask
        }
    }
}
