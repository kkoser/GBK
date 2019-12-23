package com.kkoser.emulatorcore.testing

import com.kkoser.emulatorcore.gpu.Renderer

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.math.abs

class LcdDisplay : JPanel(), Renderer {
    private val img = BufferedImage(160 * 2, 144 * 2, BufferedImage.TYPE_INT_RGB)
    //    private static final int[] COLORS = new int[] {Color.WHITE.getRGB(), Color.LIGHT_GRAY.getRGB(), Color.DARK_GRAY.getRGB(), Color.BLACK.getRGB()};
    override fun render(x: Int, y: Int, color: com.kkoser.emulatorcore.gpu.Color) {
//        println("drawing at $x,$y with color ${color.red}")
        img.setRGB(x * 2, y * 2, colorForSprint(color))
        img.setRGB(x * 2, y * 2 + 1, colorForSprint(color))
        img.setRGB(x * 2 + 1, y * 2, colorForSprint(color))
        img.setRGB(x * 2 + 1, y * 2 + 1, colorForSprint(color))
    }

    override fun refresh() {
        validate()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g.create() as Graphics2D
        g2d.drawImage(img, 0, 0, Color.WHITE, null)
        g2d.dispose()
    }

    private fun colorForSprint(color: com.kkoser.emulatorcore.gpu.Color): Int {
        return Color(abs(255 -color.red), abs(255 - color.blue), abs(255 - color.green)).rgb
    }
}