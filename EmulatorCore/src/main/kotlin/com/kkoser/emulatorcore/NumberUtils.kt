package com.kkoser.emulatorcore

fun Byte.toUnsignedInt(): Int = (this.toInt() and 0xFF)

fun Int.toUnsigned8BitInt(): Int = (this and 0xFF)
fun Int.toUnsigned16BitInt(): Int = (this and 0xFFFF)

fun Int.toIntWithLowerInt(lowerInt: Int): Int {
    return ((this.toUnsigned8BitInt() shl 8) or lowerInt.toUnsigned8BitInt()).toUnsigned16BitInt()
}

fun Int.toIntWithHighNibble(highNibble: Int): Int {
    return (this or ((highNibble shl 4) and 0xf0)).toUnsigned8BitInt()
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
    return ((this.toUnsigned8BitInt()) and 0xF0) ushr 4
}

fun Int.checkBit(bit: Int): Boolean {
    return ((1 shl bit) and this) > 0
}

fun Int.getBit(bit: Int): Int {
    return ((1 shl bit) and this) ushr (bit)
}

fun Int.is8BitNegative(): Boolean = (this and (1 shl 7)) != 0

private fun Int.abs8Bit(): Int {
    return if (this.is8BitNegative()) {
        0x100 - this
    } else {
        this
    }
}

fun Int.add8BitSigned(signedValue: Int): Int {
    val absValue = signedValue.abs8Bit()
    if (signedValue.is8BitNegative()) {
        return (this - absValue).toUnsigned16BitInt()
    }
    return (this + absValue).toUnsigned16BitInt()
}

fun Int.toHexString(): String {
    return String.format("%04x", this)
}