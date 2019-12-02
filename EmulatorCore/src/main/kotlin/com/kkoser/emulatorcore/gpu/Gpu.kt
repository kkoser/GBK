package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.toHexString
import java.lang.RuntimeException

data class Color(val red: Int, val green: Int, val blue: Int)

interface Renderer {
    fun render(x: Int, y: Int, color: Color)
}

class NoOpRenderer : Renderer {
    override fun render(x: Int, y: Int, color: Color) {

    }
}

class Gpu(val lcd: Lcd, val renderer: Renderer) {

    var bgPallete: Int = 0
    var obj0Pallete: Int = 0
    var obj1Pallete: Int = 0

    private val vram = Array(8192, { 0 })
    // OAM memory holds 40 sprites at 4 bytes each, so 160 bytes
    private val oamRam = Array(160, { 0 })

    fun tick(cyclesTaken: Int) {

    }

    fun read(location: Int): Int {
        // vram is accessible anytime other than transfer
        if (lcd.mode == Lcd.Mode.H_BLANK || lcd.mode == Lcd.Mode.OAM_SEARCH) {
            when (location) {
                in 0x8000..0x9FFF -> {
                    return vram[location - 0x8000]
                }
            }
        }

        // oam ram is only accessible in v blank
        if (lcd.mode == Lcd.Mode.V_BLANK) {
            when (location) {
                in 0x8000..0x9FFF -> {
                    return vram[location - 0x8000]
                }
                in 0xFE00..0xFE9F -> {
                    // OAM
                    return oamRam[location - 0xFE00]
                }
            }
        }

        return 0
//        throw RuntimeException("Trying to read from vram outside of v-blank")
    }

    fun write(location: Int, value: Int) {
//        throw RuntimeException("Writing to vram at location ${location.toHexString()}")
        if (lcd.mode == Lcd.Mode.V_BLANK) {
            when (location) {
                in 0x8000..0x9FFF -> {
                    vram[location - 0x8000] = value
                }
                in 0xFE00..0xFE9F -> {
                    // OAM
                    throw RuntimeException("filling oam ram")
                    oamRam[location - 0xFE00] = value
                }
            }
        }

//        throw RuntimeException("Trying to write to vram outside of v-blank")
        // outside v-blank this is a no-op
    }
}