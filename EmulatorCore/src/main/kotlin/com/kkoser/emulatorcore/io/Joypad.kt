package com.kkoser.emulatorcore.io

import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.memory.MemoryBus


class Joypad(val interruptHandler: InterruptHandler, val memory: MemoryBus) {
    companion object {
        private const val MEMORY_LOCATION = 0xFF00
    }

    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    fun aPressed() {
        val value = constructValue(true, 0)
        memory.write(MEMORY_LOCATION, value)
        interruptHandler.interrupt(InterruptHandler.Interrupt.JOYPAD)
    }

    fun bPressed() {
        val value = constructValue(true, 1)
        memory.write(MEMORY_LOCATION, value)
        interruptHandler.interrupt(InterruptHandler.Interrupt.JOYPAD)
    }

    fun startPressed() {
        val value = constructValue(true, 3)
        memory.write(MEMORY_LOCATION, value)
        interruptHandler.interrupt(InterruptHandler.Interrupt.JOYPAD)
    }

    fun selectPressed() {
        val value = constructValue(true, 2)
        memory.write(MEMORY_LOCATION, value)
        interruptHandler.interrupt(InterruptHandler.Interrupt.JOYPAD)
    }

    fun dpadPressed(direction: Direction) {
        val directionValue = when (direction) {
            Direction.RIGHT -> 0
            Direction.LEFT -> 1
            Direction.UP -> 2
            Direction.DOWN -> 3
        }
        val value = constructValue(false, directionValue)
        memory.write(MEMORY_LOCATION, value)
        interruptHandler.interrupt(InterruptHandler.Interrupt.JOYPAD)
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
    private fun constructValue(isButtonInput: Boolean, value: Int): Int {
        if (isButtonInput) {
            return 0b100000.inv() and (1 shl value).inv()
        } else {
            return 0b10000.inv() and (1 shl value).inv()
        }
    }
}

