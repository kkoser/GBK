package com.kkoser.emulatorcore

fun Byte.toUnsignedInt(): Int = (this.toInt() and 0xFF)

fun Int.toUnsigned8BitInt(): Int = (this and 0xFF)
fun Int.toUnsigned16BitInt(): Int = (this and 0xFFFF)

fun Int.toIntWithLowerInt(lowerInt: Int): Int {
    return ((this shl 8).toUnsigned16BitInt() or lowerInt.toUnsigned8BitInt()).toUnsigned16BitInt()
}

fun Int.getLow8Bits(): Int {
    return this and 0xFF
}

fun Int.getHigh8Bits(): Int {
    return (this ushr 8).getLow8Bits()
}

fun Int.checkBit(bit: Int): Boolean {
    return ((1 shl bit) and this) > 0
}

fun Int.toHexString(): String {
    return Integer.toHexString(this)
}