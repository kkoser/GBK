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
import com.kkoser.emulatorcore.io.Joypad
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.CartridgeFactory
import com.kkoser.emulatorcore.memory.MemoryBus
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

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

                val gameFile = File("/Users/kkoser/Projects/GBK/test.gb")
//                val gameFile = File("/Users/kkoser/Downloads/Dr. Mario (World).gb")
//                val gameFile = File("/Users/kkoser/Downloads/Pokemon Trading Card Game (U) [C][!].gbc")
//                val gameFile = File("/Users/kkoser/Downloads/Kirby's Dream Land (USA, Europe).gb")
//                val gameFile = File("/Users/kkoser/Downloads//Pokemon Red (U) [S][BF].gb") // prints invalid character
//                val gameFile = File("/Users/kkoser/Downloads/01-special.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/06-ld r,r.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/10-bit ops.gb") // PASS
//                val gameFile = File("/Use/rs/kkoser/Downloads/11-op a,(hl).gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/09-op r,r.gb") // pass
//                val gameFile = File("/Users/kkoser/Downloads/08-misc instrs.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/07-jr,jp,call,ret,rst.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/05-op rp.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/04-op r,imm.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/03-op sp,hl.gb") // PASS
//                val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/individual/02-interrupts.gb") // PASS
//                val gameFile = File("/Users/kkoser/Downloads/daa.gb") // FAIL with weird graphics issues
//                val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/source/test.gb")
//                val gameFile = File("/Users/kkoser/Projects/gb-test-roms/cpu_instrs/cpu_instrs.gb") // infinite increasing numbers?
//                val gameFile = File("/Users/kkoser/Downloads/dmg-acid2.gb")
//                val gameFile = File("/Users/kkoser/Downloads/mooneye/acceptance/bits/reg_f.gb")
//                val gameFile = File("/Users/kkoser/Downloads/mooneye/emulator-only/mbc1/rom_512kb.gb")
//                val gameFile = File("/Users/kkoser/Downloads/1-lcd_sync.gb")

                val rom = CartridgeFactory.getCartridgeForFile(gameFile)
                val timer = Timer()
                val lcd = Lcd()
                val gpu = Gpu(lcd, display, debugDisplay)
                val dma = Dma()
                val interruptHandler = DefaultInterruptHandler()
                val joyPad = Joypad(interruptHandler)
                val memoryBus = MemoryBus(rom, timer, interruptHandler, lcd, dma, gpu, joyPad)
                val cpu = Cpu(memoryBus, true)
                val emulator = Emulator(cpu, memoryBus, interruptHandler, timer, lcd)


                mainWindow.addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) {

                    }

                    override fun keyPressed(e: KeyEvent?) {
                        when (e?.keyCode) {
                            KeyEvent.VK_DOWN -> joyPad.dpadPressed(Joypad.Direction.DOWN)
                            KeyEvent.VK_UP -> joyPad.dpadPressed(Joypad.Direction.UP)
                            KeyEvent.VK_LEFT -> joyPad.dpadPressed(Joypad.Direction.LEFT)
                            KeyEvent.VK_RIGHT -> joyPad.dpadPressed(Joypad.Direction.RIGHT)

                            KeyEvent.VK_ENTER -> joyPad.startPressed()
                            KeyEvent.VK_BACK_SPACE -> joyPad.selectPressed()
                            KeyEvent.VK_Z -> joyPad.aPressed()
                            KeyEvent.VK_X -> joyPad.bPressed()
                        }
                    }

                    override fun keyReleased(e: KeyEvent?) {
                        when (e?.keyCode) {
                            KeyEvent.VK_DOWN -> joyPad.dpadPressed(Joypad.Direction.DOWN, true)
                            KeyEvent.VK_UP -> joyPad.dpadPressed(Joypad.Direction.UP, true)
                            KeyEvent.VK_LEFT -> joyPad.dpadPressed(Joypad.Direction.LEFT, true)
                            KeyEvent.VK_RIGHT -> joyPad.dpadPressed(Joypad.Direction.RIGHT, true)

                            KeyEvent.VK_ENTER -> joyPad.startPressed(true)
                            KeyEvent.VK_BACK_SPACE -> joyPad.selectPressed(true)
                            KeyEvent.VK_Z -> joyPad.aPressed(true)
                            KeyEvent.VK_X -> joyPad.bPressed(true)
                        }
                    }
                })

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