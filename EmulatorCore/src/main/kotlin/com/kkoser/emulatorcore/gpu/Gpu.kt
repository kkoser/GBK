package com.kkoser.emulatorcore.gpu

import com.kkoser.emulatorcore.checkBit
import com.kkoser.emulatorcore.getBit
import com.kkoser.emulatorcore.toSigned8BitInt
import com.kkoser.emulatorcore.toUnsigned8BitInt
import org.jetbrains.annotations.Contract
import java.lang.RuntimeException
import kotlin.math.abs

private val VRAM_START = 0x8000

data class Color(val red: Int, val green: Int, val blue: Int) {
    companion object{
        val WHITE = Color(0,0,0)
        val LIGHT_GREY = Color(52, 104, 86)
        val DARK_GREY = Color(136,192,112)
        val BLACK = Color(255, 255, 255)

        fun getColorFromGbId(id: Int, pallette: Int): Color? {
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
                    oamRam[location - 0xFE00] = value
                }
            }
        } else {
//            throw RuntimeException("trying to write to vramoutside vblank")
        }
    }

    data class Tile(val id: Int, val values: Array<Int>) {
        @Contract("_, _, _, false -> !null")
                /**
                 * Returns the color of the pixel with the given palette, if it is a transparent
                 * pixel in a sprite returns null
                 */
        fun getPixel(x: Int, y: Int, pallette: Int, sprite: Boolean = false): Color? {
            val upper = values[2 * y]
            val lower = values[(2 * y) + 1]

            val lowerColorBit = upper.getBit(abs(7 - x))
            val upperColorBit = lower.getBit(abs(7 - x))

            val colorValue = (upperColorBit shl 1) or lowerColorBit

            // 00 in sprite palette is always transparent
            if (colorValue == 0 && sprite) {
                return null
            }

            // Need to do the color decode
            val lowerBitLocation = 2 * colorValue
            val higherBitLocation = lowerBitLocation + 1
            val lowerDecodedColor = pallette.getBit(lowerBitLocation)
            val higherDecodedColor = pallette.getBit(higherBitLocation)
            val colorId = (higherDecodedColor shl 1) or lowerDecodedColor

            return Color.getColorFromGbId(colorId, pallette)
        }
    }

    data class Sprite(val id: Int, val x: Int, val y: Int, val tileId: Int, val pallette: Int) {
        companion object {
            // http://hisimon.dk/misc/pandocs/#vram-sprite-attribute-table-oam for details on sprite format
            fun fromOamData(data: Array<Int>, id: Int): Sprite {
                val offset = id * 4
                val flags = data[offset + 3]
                return Sprite(
                    id = id,
                    x = data[offset + 1] - 8,
                    y = data[offset + 0] - 16,
                    tileId = data[offset + 2],
                    pallette = flags.getBit(4)
                )
            }
        }
    }

    private fun lcdTransfer(line: Int) {
        val scrollYTileShift = lcd.scrollY / 8

        val yStart = (line + lcd.scrollY) % 255 // wrap around the window if needed
        val xStart = lcd.scrollX

        val backgroundMap = getBackgroundMap()
        val sprites = getSpritesForLine(line)

        // Psuedoccode
        // Find spritetes for line
        // for x in line:
        // Find sprite thatt draws there, if any
        // If present and not transparent draw it
        // else find bg pixel at that spot and draw it

        for (x in 0 until 160) {
            // Sprites are always 8 wide
            val sprite = if (isOAMEnaabled()) sprites.find { it.x <= x && it.x + 8 > x } else null
            if (sprite == null) {
                if (isBackgroundEnabled()) {
                    renderer.render(x, line, getBackgroundPixelAtLocation(line, x, backgroundMap))
                }
                continue
            }
            val spriteTile = getSpriteTileFromId(sprite.tileId)
            val color = spriteTile.getPixel(x - sprite.x, line - sprite.y, getSpritePallette(sprite), true)
            if (color == null) {
                renderer.render(x, line, getBackgroundPixelAtLocation(line, x, backgroundMap))
            } else if (isBackgroundEnabled()){
                renderer.render(x, line, color)
            }
        }

        // Only update the map on the first draw, as it draws the entire map independently of the current scan line
        if (line == 0) {
            renderer.refresh()
            renderDebugTiles()
        }
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
                    debugRenderer.render(x + xOffset, y+ yOffset, it.value.getPixel(x, y, bgPallete)!!)
                }
            }
        }

        debugRenderer.refresh()
    }

    private fun getSpritesForLine(line: Int): List<Sprite> {
        val allSprites = (0..39).map { Sprite.fromOamData(oamRam, it) }
        return allSprites.filter { it.y < line && line < it.y + getSpriteHeight() && it.x > 0 && it.x < 160 }.take(10)
    }

    private fun getBackgroundPixelAtLocation(line: Int, x: Int, backgroundMap: Array<Array<Int>>): Color {
        val yStart = (line + lcd.scrollY) % 255 // wrap around the window if needed
        val xStart = (lcd.scrollX + x) % 255 // wrap around if needed
        // Determine the row of BG tiles that corresponds to the given line (keep in mind tiles are
        // all 8x8)
        val tileRow = yStart / 8
        val tileColumn = xStart / 8

        // Fetch the row from the BG map, there are 20 tiles across the screen at a time
        val tileId = backgroundMap[tileRow][tileColumn]
        val tile = getTileFromId(tileId)

        val xInTile = xStart % 8
        val yInTile = yStart % 8
        // bg cannot be transparent
        return requireNotNull(tile.getPixel(xInTile, yInTile, bgPallete))
    }

    // In non-collor (CGB) mode, theres only one OAM bank
    // TODO support bank switching for CGB mode
    private fun getSpriteTileFromId(id: Int): Tile {
        // Each tile is 16 bytes in memory
        // Subtract VRAM_START because we arent going through the databus

        // OAM sprites in non-color mode are always in 0x8000-0x8fff
        val offset = (id.toUnsigned8BitInt() * 16)
        return Tile(id, vram.sliceArray(IntRange(offset, offset + 15)))
    }

    private fun getTileFromId(id: Int): Tile {
        // Each tile is 16 bytes in memory
        // Subtract VRAM_START because we arent going through the databus
        if (lcd.control.checkBit(4)) {
            // The base for tiles here is 0x8000, which is also the start of VRAM
            // Since we arent going through the memory bus, this washes out when we access VRAM
            val offset = (id * 16)
            return Tile(id, vram.sliceArray(IntRange(offset, offset + 15)))
        }

        // We're in the 8800 base mode, so the value is now signed
        val offset = (id.toSigned8BitInt() * 16) + 0x9000 - VRAM_START
        return Tile(id, vram.sliceArray(IntRange(offset, offset + 15)))
    }

    // TODO: Support different map modes (bg vs window)
    private fun getBackgroundMap(): Array<Array<Int>> {
        // Subtract the start of VRAM since we aren't going through the memorybus for this
        val BG_START = getBackgroundMapBase() - VRAM_START
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
            0x9C00
        else
            0x9800
    }

    // Sprites are 8x8 or 8x16
    private fun getSpriteHeight(): Int {
        return if (lcd.control.checkBit(2))
            throw RuntimeException("16 bit sprites not supported")
        else
            8
    }

    private fun getSpritePallette(sprite: Sprite): Int {
        return if (sprite.pallette == 0)
            obj0Pallete
        else
            obj1Pallete
    }

    private fun isOAMEnaabled(): Boolean {
        return lcd.control.checkBit(1)
    }

    private fun isWindowEnabled(): Boolean {
        return lcd.control.checkBit(5)
    }
}