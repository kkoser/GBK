package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.toUnsignedInt

class MBC3(bytes: ByteArray) : CartridgeMemory {

    companion object {
        const val BANK_DIVIDE = 0x4000
        const val RAM_BANK_SIZE = 0xBFFF - 0xA000
        const val NUM_RAM_BANKS = 8
    }

    private val fullMemory: Array<Int> = Array(bytes.size, { i: Int -> bytes[i].toUnsignedInt() })
    private val fullRam: Array<Int> = Array(RAM_BANK_SIZE * NUM_RAM_BANKS, { 0 })
    private var selectedBank: Int = 1
        set(value) {
            // If you write 0 here, you still select bank1 (since bank 0 is always accessible)
            if (value == 0x00) {
                field = 0x01
            } else {
                field = value
            }
        }
    private var selectedRamBank = 0
    private var ramEnabled = false

    override fun read(position: Int): Int {
        when (position) {
            in 0..0x3FFF -> {
                return fullMemory[position]
            }
            in 0x4000..0x7FFF -> {
                if (!ramEnabled) {
                    return 0
                }
                // Selectable bank
                val actualPosition = position + (selectedBank * BANK_DIVIDE)
                return fullMemory[actualPosition]
            }
            in 0xA000..0xBFFF -> {
                // ram bank
                val pos = position - 0xA000
                return fullRam[pos + (selectedRamBank * RAM_BANK_SIZE)]
            }
        }
        return 0
    }

    override fun write(position:Int, value: Int) {
        // Intercept to control the changing of ram and rom bank changes
        when (position) {
            in 0x0000..0x1FFF -> {
                // RAM / timer enable
                ramEnabled = value != 0
            }
            in 0x2000..0x3FFF -> {
                selectedBank = value
            }
            in 0x4000..0x5FFF -> {
                // RAM bank select or RTC select
                // TODO: RTC
                if (value in 0x00..0x07) {
                    selectedRamBank = value
                }
            }
        }
    }
}