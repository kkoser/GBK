package com.kkoser.emulatorcore.cpu

/**
 * An Operation can make changes to the Cpu
 *
 * @return The number of cycles this operation should take
 */
typealias Operation = (Cpu) -> Int

object OpCodes {
    val opCodes = mapOf<Int, Operation>(
            0x0 to Cpu::noop
    )
}