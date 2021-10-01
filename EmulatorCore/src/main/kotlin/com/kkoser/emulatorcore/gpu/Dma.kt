package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.memory.MemoryBus

/**
 * The DMA is used so the cpu can prepare vram in other areas of ram, and then
 * copy it over to the gpu when the gpu is ready for it. Essentially, it just copies chunks
 * of memory into a new location
 */
class Dma {

    private val TRANSFER_CYCLES = 160
    private val OAM_SIZE = 160
    private val OAM_BASE = 0xFE00

    private var cycles = 0
    private var baseWriteLocation = 0
    private var writeProgress = 0

    var isWriting = false
        private set
    /**
     * When we write to the DMA register, it kicks off a copy
     * The location to copy from is the location written to * 100
     *
     * Copies into sprite memory at 0xFE00-0xFE9F
     */
    fun write(writtenLocation: Int) {
        isWriting = true
        val startLocation = writtenLocation * 0x100
        baseWriteLocation = startLocation
        writeProgress = 0
    }

    fun tick(cyclesTaken: Int, memoryBus: MemoryBus) {
        if (isWriting) {
            cycles += cyclesTaken

            if (cycles >= TRANSFER_CYCLES) {
                cycles = 0
                isWriting = false
                for (i in 0 until OAM_SIZE) {
                    memoryBus.write(OAM_BASE + i, memoryBus.read(baseWriteLocation + i))
                }
            }
        }
    }
}