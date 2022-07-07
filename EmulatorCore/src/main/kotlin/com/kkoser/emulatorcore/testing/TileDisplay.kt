package com.kkoser.emulatorcore.testing

import com.kkoser.emulatorcore.gpu.BackgroundInfoRenderer
import com.kkoser.emulatorcore.gpu.Renderer
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.abs

class TileDisplay : JFrame(), BackgroundInfoRenderer {
//    private val img = BufferedImage(160 * 2, 144 * 2, BufferedImage.TYPE_INT_RGB)
    private val tv = TextArea("Hello world")

    init {
        add(tv)
        tv.setBounds(0, 0, 200, 200);
    }
    //    private static final int[] COLORS = new int[] {Color.WHITE.getRGB(), Color.LIGHT_GRAY.getRGB(), Color.DARK_GRAY.getRGB(), Color.BLACK.getRGB()};
    override fun drawBgInfo(bgInfo: String) {
        tv.text = bgInfo
    }

    override fun refresh() {
        validate()
        repaint()
    }
//
//    override fun paintComponent(g: Graphics) {
//        super.paintComponent(g)
//        val g2d = g.create() as Graphics2D
//        g2d.drawImage(img, 0, 0, Color.WHITE, null)
//        g2d.dispose()
//        g.
//    }
}