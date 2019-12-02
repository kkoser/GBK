package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.memory.MemoryBus

/**
 * The DMA is used so the cpu can prepare vram in other areas of ram, and then
 * copy it over to the gpu when the gpu is ready for it. Essentially, it just copies chunks
 * of memory into a new location
 */
class Dma {
    /**
     * When we write to the DMA register, it kicks off a copy
     * The location to copy from is the location written to * 100
     *
     * Copies into sprite memory at 0xFE00-0xFE9F
     */
    fun write(writtenLocation: Int, memoryBus: MemoryBus) {
        val startLocation = writtenLocation * 100
        val oamBase = 0xFE00
        for (i in 0x00..0xA0) {
            memoryBus.write(oamBase + i, startLocation + i)
        }
    }
}