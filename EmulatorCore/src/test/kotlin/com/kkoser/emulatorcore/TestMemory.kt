package com.kkoser.emulatorcore

import com.kkoser.emulatorcore.memory.CartridgeMemory
import com.kkoser.emulatorcore.toUnsigned8BitInt

class TestMemory : CartridgeMemory {
    private var memory = arrayOf(
            0x06, 0xCC // load CC into B
    )

    override fun read(position: Int): Int {
        return memory[position].toUnsigned8BitInt()
    }

    override fun readSigned(position: Int): Int {
        return memory[position]
    }

    override fun write(position: Int, value: Int) {
        memory[position] = value
    }

    fun setROM(vals: Array<Int>) {
        memory = vals
    }
}