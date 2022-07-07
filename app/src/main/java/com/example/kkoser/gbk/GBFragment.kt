package com.example.kkoser.gbk

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kkoser.emulatorcore.Emulator
import com.kkoser.emulatorcore.Timer
import com.kkoser.emulatorcore.cpu.Cpu
import com.kkoser.emulatorcore.cpu.DefaultInterruptHandler
import com.kkoser.emulatorcore.gpu.Dma
import com.kkoser.emulatorcore.gpu.Gpu
import com.kkoser.emulatorcore.gpu.Lcd
import com.kkoser.emulatorcore.gpu.NoOpRenderer
import com.kkoser.emulatorcore.io.Joypad
import com.kkoser.emulatorcore.memory.BasicROM
import com.kkoser.emulatorcore.memory.MemoryBus

class GBFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_g_b, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameFile = requireActivity().assets.open("03-op sp,hl.gb")


        // Create the emulator
//        val renderer = view.findViewById<RendererImpl>(R.id.render_image_view)
        val renderer = NoOpRenderer
        val rom = BasicROM(gameFile.readBytes())
        val timer = Timer()
        val lcd = Lcd()
        val gpu = Gpu(lcd, renderer)
        val dma = Dma()
        val interruptHandler = DefaultInterruptHandler()
        val joyPad = Joypad(interruptHandler)
        val memoryBus = MemoryBus(rom, timer, interruptHandler, lcd, dma, gpu, joyPad = joyPad)
        val cpu = Cpu(memoryBus, true)
        val emulator = Emulator(cpu, memoryBus, interruptHandler, timer, lcd, dma = dma)

        emulator.run()

        if (false) blah()
    }

    fun blah() {
        Log.e("kyle", "asdf")
    }
}
