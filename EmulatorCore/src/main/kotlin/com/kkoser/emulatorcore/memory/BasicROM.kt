package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.toUnsignedInt
import java.io.File

class BasicROM(val file: File) : CartridgeMemory {

    private val memory: ByteArray

    init {
        memory = file.readBytes()
    }

    override fun read(position: Int): Int {
        return memory[position].toUnsignedInt()
    }

    override fun write(position:Int, value: Int) {
        memory[position] = value.toByte()
    }
}