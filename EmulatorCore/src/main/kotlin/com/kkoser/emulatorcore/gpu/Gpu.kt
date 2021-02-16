package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.getBit
import java.lang.RuntimeException
import kotlin.math.abs

data class Color(val red: Int, val green: Int, val blue: Int) {
    companion object{
        val WHITE = Color(0,0,0)
        val LIGHT_GREY = Color(52, 104, 86)
        val DARK_GREY = Color(136,192,112)
        val BLACK = Color(255, 255, 255)

        fun getColorFromGbId(id: Int, pallette: Int): Color {
            val decodedIdUpper = pallette.getBit(2*id + 1)
            val decodedIdLower = pallette.getBit(2*id)
            val decodedId = (decodedIdUpper shl 1) or decodedIdLower

            return when(decodedId) {
                0 -> WHITE
                1 -> LIGHT_GREY
                2 -> DARK_GREY
                3 -> BLACK
                else -> throw RuntimeException("Invalid color id $id")
            }
        }
    }
}

interface Renderer {
    fun render(x: Int, y: Int, color: Color)
    fun refresh()
}

class NoOpRenderer : Renderer {
    override fun render(x: Int, y: Int, color: Color) {
//        println( "Drawinng pixel at $x,$y with color ${color.red}")
    }

    override fun refresh() {

    }
}

class Gpu(val lcd: Lcd, val renderer: Renderer, val debugRenderer: Renderer? = null) {

    init {
        lcd.modeListener = {
            when(it) {
                Lcd.Mode.OAM_SEARCH -> {
                    // TODO: Sprite calculations
                }
                Lcd.Mode.LCD_TRANSFER -> {
                    // WORK TIME
                    lcdTransfer(lcd.currentScanLine)
                }
                Lcd.Mode.H_BLANK -> {
                    // No-op for drawing
                }
                Lcd.Mode.V_BLANK -> {
                    // No=op for drawing
                }
            }
        }
    }

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

//        return 0
        throw RuntimeException("Trying to read from vram outside of v-blank")
    }

    fun write(location: Int, value: Int) {
//        throw RuntimeException("Writing to vram at location ${location.toHexString()}")
        if (lcd.mode == Lcd.Mode.V_BLANK || !lcd.enabled()) {
            when (location) {
                in 0x8000..0x9FFF -> {
                    vram[location - 0x8000] = value
//                    if (value != 0) throw RuntimeException("writing vram at location ${location.toHexString()} value ${value.toHexString()}")
                }
                in 0xFE00..0xFE9F -> {
                    // OAM
//                    throw RuntimeException("filling oam ram")
                    oamRam[location - 0xFE00] = value
                }
            }
        } else {
//            throw RuntimeException("trying to write to vramoutside vblank")
        }
    }

    data class Tile(val id: Int, val values: Array<Int>) {
        fun getPixel(x: Int, y: Int, pallette: Int): Color {
            val upper = values[2*y]
            val lower = values[(2*y)+1]

            val lowerColorBit = upper.getBit(abs(7-x))
            val upperColorBit = lower.getBit(abs(7-x))

            val colorValue = (upperColorBit shl 1) or lowerColorBit

            // Need to do the color decode
            val lowerBitLocation = 2*colorValue
            val higherBitLocation = lowerBitLocation + 1
            val lowerDecodedColor = pallette.getBit(lowerBitLocation)
            val higherDecodedColor = pallette.getBit(higherBitLocation)
            val colorId = (higherDecodedColor shl 1) or lowerDecodedColor

            return Color.getColorFromGbId(colorId, pallette)
        }
    }

    private fun lcdTransfer(line: Int) {
        val scrollYTileShift = lcd.scrollY / 8

        val yStart = (line + lcd.scrollY) % 255 // wrap around the window if needed
        val xStart = lcd.scrollX

        if (isBackgroundEnabled()) {
            drawBackground(yStart, xStart, line)
        }
//        println("Starting lcd transfer for line with scrolly y ${lcd.scrollY} -> $yStart x $xStart")



        // Only update the map on the first draw, as it draws the entire map independently of the current scan line
        if (line == 0) {
            renderDebugTiles()
        }
    }

    private fun drawBackground(yStart: Int, xStart: Int, line: Int) {
        // We get the whole map, but the actual screen is 160x144, or 20x18 tiles
        val backgroundMap = getBackgroundMap()

        // Determine the row of BG tiles that corresponds to the given line (keep in mind tiles are
        // all 8 lines tall)
        val tileNumber = yStart / 8
//        println("Resolved to BG y location ${tileNumber.toHexString()}")

        // Fetch the row from the BG map, there are 20 tiles across the screen at a time
        val tileArr = backgroundMap[tileNumber].copyOfRange(xStart, xStart + 20)

        // Iterate over the row and get all the tiles at this level
        val tileValues = tileArr.map { tileId ->
            if (tileId != 0 && tileId != 20) {
//                println()
            }
            getTileFromId(tileId)
        }

//        println("Drawing line with tileids: ${tileArr.joinToString(postfix = ";")}")

        // Determine which part of the tile we want to draw for the given line (tiles are 8px tall)
        val lineInTile = yStart % 8

        // iterate over found tiles and request draws of correct pixels at this line
        for ((index, tile) in tileValues.withIndex()) {
            for (x in 0..7) {
                renderer.render((8 * index) + x, line, tile.getPixel(x, lineInTile, bgPallete))
            }
        }

        // request new line be shown on screen
        renderer.refresh()
    }

    fun renderDebugTiles() {
        if (debugRenderer == null)
            return

        val allTiles = (0..255).map {
            getTileFromId(it)
        }.toTypedArray()

        allTiles.withIndex().map {
            // Get the number it is on this line, then multiple by 8 as each tile is 8 px wide
            val xOffset = (it.index % 16) * 8
            val yOffset = (it.index / 16) * 8
            for (x in 0..7) {
                for (y in 0..7) {
                    debugRenderer.render(x + xOffset, y+ yOffset, it.value.getPixel(x, y, bgPallete))
                }
            }
        }

        debugRenderer.refresh()
    }

    private fun getTileFromId(id: Int): Tile {
        // Each tile is 16 bytes in memory
        val offset = id * 16
//        val useNormalMap = lcd.status.checkBit(6)
        return Tile(id, vram.sliceArray(IntRange(offset, offset + 15)))
    }

    // TODO: Support different map modes (bg vs window)
    private fun getBackgroundMap(): Array<Array<Int>> {
        // Subtract the start of VRAM since we aren't going through the memorybus for this
        val BG_START = 0x9800 - 0x8000
        val BG_LENGTH = 32

        val ret = Array<Array<Int>>(32) {Array(32) {0}}

        for (i in 0 until BG_LENGTH) {
            val arr = ret[i]
            for (j in 0 until BG_LENGTH) {
                arr[j] = vram[BG_START + (32*i) + j]
            }
        }

        return ret
    }

    private fun isBackgroundEnabled(): Boolean {
        return lcd.control.checkBit(0)
    }

    private fun getBackgroundMapBase(): Int {
        return if (lcd.control.checkBit(3))
            5
        else
            5
    }

    private fun isOAMEnaabled(): Boolean {
        return lcd.control.checkBit(1)
    }

    private fun isWindowEnabled(): Boolean {
        return lcd.control.checkBit(5)
    }
}