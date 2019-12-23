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
            Thread {
                //                new Emulator(display).run();

                val gameFile = File("/Users/kkoser/Projects/GBK/test.gb")
//                val gameFile = File("/Users/kkoser/Downloads/01-special.gb")

                val rom = BasicROM(gameFile)
                val timer = Timer()
                val lcd = Lcd()
                val gpu = Gpu(lcd, display)
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