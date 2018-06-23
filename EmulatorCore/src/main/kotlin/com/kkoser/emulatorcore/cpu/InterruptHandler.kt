package com.kkoser.emulatorcore.cpu

interface InterruptHandler {

    /**
     * All possible interrupts that can be requested and serviced in the game boy architecture
     *
     * @note The ordinal values of this enumerated type also represent the bit for this interrupt in
     * the IE and IF flags. Ex: V_BLANK is the first bitin the register, LCD is the second bit, and so on.
     * So, the mask can be caluclated as (1 shl (ordinal + 1))
     */
    enum class Interrupt(val location: Int) {
        V_BLANK(0x40),
        LCD(0x48),
        CLOCK(0x50),
        SERIAL(0x58),
        JOYPAD(0x60);

        /**
         * Gets the mask for this interrupt for the IE/IF registers. The ordinal value of the interrupt
         * also represents the bit position of that item in the register, so the mask can be calculated
         * by shifting 1 left ordinal + 1 times
         */
        fun mask(): Int {
            return (1 shl (ordinal + 1))
        }
    }

    var registerIE: Int

    var registerIF: Int

    fun interrupt(interrupt: Interrupt)

    fun handleInterrupts(cpu: Cpu)

    fun toggleInterrupt(interrupt: Interrupt)
}

class DefaultInterruptHandler : InterruptHandler {
    /**
     * Interrupt enable register - controls whether an interrupt type is turned on
     */
    override var registerIE = 0b00000

    /**
     * Interrupt Request Register- lists what interrupts are pending processing currently
     */
    override var registerIF = 0b00000

    override fun interrupt(interrupt: InterruptHandler.Interrupt) {
        registerIF = registerIF or interrupt.mask()
    }

    override fun handleInterrupts(cpu: Cpu) {
        if (!cpu.ime) return

        InterruptHandler.Interrupt.values().forEach { value ->
            if (registerIF and value.mask() == value.mask()) {
                if (registerIE and value.mask() == value.mask()) {
                    cpu.interrupt(value)
                    clearInterrupt(value)
                }
            }
        }
    }

    override fun toggleInterrupt(interrupt: InterruptHandler.Interrupt) {
        registerIE = registerIF or interrupt.mask()
    }

    private fun clearInterrupt(interrupt: InterruptHandler.Interrupt) {
        registerIE = registerIF and interrupt.mask().inv()
    }
}

// CPU Interrupt handler methods

fun Cpu.interrupt(interrupt: InterruptHandler.Interrupt) {
    ime = false
    push(pc)
    pc = interrupt.location
}