package com.kkoser.emulatorcore.memory

interface CartridgeMemory {
    fun read(position: Int): Int
    fun readSigned(position: Int): Int

    fun write(position:Int, value: Int)
}