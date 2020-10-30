package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.toUnsignedInt
import java.io.File

class MBC1(val file: File) : CartridgeMemory {

    companion object {
        const val BANK_DIVIDE = 0x4000
    }

    private val fullMemory: Array<Int>
    private var selectedBank: Int = 1
    set(value) {
        // Need to handle these custom values that map to something different
        if (value == 0x00) field = 0x01
        if (value == 0x20) field = 0x21
        if (value == 0x40) field = 0x41
        if (value == 0x60) field = 0x61

        field = value
    }

    init {
        val bytes = file.readBytes()
        fullMemory = Array(bytes.size, { i: Int -> bytes[i].toUnsignedInt() })
    }

    override fun read(position: Int): Int {
        if (position < MBC1.BANK_DIVIDE) {
            return fullMemory[position]
        }

        val actualPosition = position + (selectedBank * BANK_DIVIDE)
        return fullMemory[actualPosition]

    }

    override fun write(position:Int, value: Int) {
        // Intercept to control the changing of ram and rom bank changes
        if (position in 0x2000..0x4000) {
            // Clear the lower 5 bits
            val bankMasked = selectedBank and 0xE0
            val lowBits = value and 0x1F

            selectedBank = bankMasked and lowBits
        }
    }
}