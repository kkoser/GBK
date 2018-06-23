package com.kkoser.emulatorcore.gpu

class Lcd {
    var currentScanLine = 0
        private set

    // The game cannot request to go to any scanline - writing any memory to 0xFF44 sets it to 0
    fun resetScanLine() {
        currentScanLine = 0
    }
}