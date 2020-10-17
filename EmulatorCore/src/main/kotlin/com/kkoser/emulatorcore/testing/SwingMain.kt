package com.kkoser.emulatorcore.testing

import com.kkoser.emulatorcore.Emulator
import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.DefaultInterruptHandler
import com.kkoser.emulatorcore.cpu.InterruptHandler
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.MemoryBus

import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

object SwingMain {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater { createAndShowGUI() }
    }

    private fun createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val mainWindow = JFrame("GB")
            mainWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            mainWindow.setLocationRelativeTo(null)
            val display = LcdDisplay()
            display.setSize(160 * 2, 144 * 2)
            mainWindow.contentPane = display
            mainWindow.isResizable = false
            mainWindow.isVisible = true
            mainWindow.setSize(160 * 2 + 10, 144 * 2 + 50)

            val vramWindow = JFrame("VRAM")
            vramWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            val debugDisplay = LcdDisplay()
            debugDisplay.setSize(256, 256)
            vramWindow.contentPane = debugDisplay
            vramWindow.isVisible = true
            vramWindow.setSize(256, 256)

            Thread {

//                val gameFile = File("/Users/kkoser/Projects/GBK/test.gb")
//                val gameFile = File("/Users/kkoser/Downloads/Dr. Mario (World).gb") //FAIL (repeatedly on DAA)
//                val gameFile = File("/Users/kkoser/Downloads/01-special.gb") //FAIL (repeatedly on DAA)
//                val gameFile = File("/Users/kkoser/Downloads/06-ld r,r.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/10-bit ops.gb") // FF FAIL
//                val gameFile = File("/Users/kkoser/Downloads/11-op a,(hl).gb") // FAIL
//                val gameFile = File("/Users/kkoser/Downloads/09-op r,r.gb") // FAIL
//                val gameFile = File("/Users/kkoser/Downloads/08-misc instrs.gb") // FAIL in loop, does not start
//                val gameFile = File("/Users/kkoser/Downloads/07-jr,jp,call,ret,rst.gb") // FAIL, does not finish
//                val gameFile = File("/Users/kkoser/Downloads/05-op rp.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/04-op r,imm.gb") // FAIL "0E 0E 06 0E"
//                val gameFile = File("/Users/kkoser/Downloads/03-op sp,hl.gb") // LOOP, does not finish
                val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/source/test.gb") // LOOP, does not finish

                val rom = BasicROM(gameFile.inputStream())
                val timer = Timer()
                val lcd = Lcd()
                val gpu = Gpu(lcd, display, debugDisplay)
                val dma = Dma()
                val interruptHandler = DefaultInterruptHandler()
                val memoryBus = MemoryBus(rom, timer, interruptHandler, lcd, dma, gpu)
                val cpu = Cpu(memoryBus, true)
                val emulator = Emulator(cpu, memoryBus, interruptHandler, timer, lcd)

                try {
                    emulator.run()
                } catch (e: Exception) {
                    Logger.getGlobal().log(Level.SEVERE, "caught exception")
                    Logger.getGlobal().log(Level.SEVERE, e.message, e.stackTrace)
                    throw e
                }

                System.exit(0)
            }.start()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}