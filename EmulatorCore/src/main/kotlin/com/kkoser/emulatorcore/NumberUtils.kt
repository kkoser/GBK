package com.kkoser.emulatorcore

fun Byte.toUnsignedInt(): Int = (this.toInt() and 0xFF)

fun Int.toIntWithLowerInt(lowerInt: Int): Int {
    return ((this shl 8) or lowerInt)
}

fun Int.getLow8Bits(): Int {
    return this and 0xFF
}

fun Int.getHigh8Bits(): Int {
    return (this ushr 8).getLow8Bits()
}