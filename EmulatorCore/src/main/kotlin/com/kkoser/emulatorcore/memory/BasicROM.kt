package com.kkoser.emulatorcore.memory

import com.kkoser.emulatorcore.toHexString
import com.kkoser.emulatorcore.toUnsigned8BitInt
import java.io.File
import java.io.InputStream
import java.util.logging.Level
import java.util.logging.Logger

class BasicROM(bytes: ByteArray) : CartridgeMemory {
    private val memory: Array<Int> = Array(bytes.size, { i: Int -> bytes[i].toInt() })

    override fun read(position: Int): Int {
        if (position >= 0xA000) {
            return 0xFF
        }

        return memory[position].toUnsigned8BitInt()
    }

    override fun write(position:Int, value: Int) {
        // They may try to write to ram, which is a no-op for this type of cartridge. We should still allow it though,
        // so we only crash for writing the ROM locations
//        if (position <= 0x8000) {
//            Logger.getGlobal().log(Level.SEVERE, "Someone is trying to write to ROM! at address ${Integer.toHexString(position)}, value ${Integer.toHexString(value)}", RuntimeException())
//            throw RuntimeException()
//        }

//        if (position >= 0xA000) {
//            ram[position - 0xA000 - 1] = value
//        }
    }
}