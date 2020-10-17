package com.kkoser.emulatorcore

fun Byte.toUnsignedInt(): Int = (this.toInt() and 0xFF)

fun Int.toUnsigned8BitInt(): Int = (this and 0xFF)
fun Int.toUnsigned16BitInt(): Int = (this and 0xFFFF)

fun Int.toIntWithLowerInt(lowerInt: Int): Int {
    return ((this.toUnsigned8BitInt() shl 8) or lowerInt.toUnsigned8BitInt()).toUnsigned16BitInt()
}

fun Int.toIntWithHighNibble(highNibble: Int): Int {
    return (this and (highNibble.toUnsigned8BitInt() shl 4)).toUnsigned8BitInt()
}

fun Int.getLow8Bits(): Int {
    return this and 0xFF
}

fun Int.getHigh8Bits(): Int {
    return (this ushr 8).getLow8Bits()
}

fun Int.getLowNibble(): Int {
    return (this.toUnsigned8BitInt()) and 0x0F
}

fun Int.getHighNibble(): Int {
    // We need to shift the high nibble down to not have four 0s at the start
    return ((this.toUnsigned8BitInt()) and 0xF0) shr 4
}

fun Int.checkBit(bit: Int): Boolean {
    return ((1 shl bit) and this) > 0
}

fun Int.getBit(bit: Int): Int {
    return ((1 shl bit) and this) shr (bit)
}

fun Int.toHexString(): String {
    return String.format("%04x", this)
}