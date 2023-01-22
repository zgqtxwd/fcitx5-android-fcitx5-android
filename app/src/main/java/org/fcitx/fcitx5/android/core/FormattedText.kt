package org.fcitx.fcitx5.android.core

import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.style.BackgroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.text.buildSpannedString
import splitties.bitflags.hasFlag

/**
 * translated from
 * [fcitx-utils/textformatflags.h](https://github.com/fcitx/fcitx5/blob/5.0.21/src/lib/fcitx-utils/textformatflags.h)
 */
enum class TextFormatFlag(val flag: Int) {
    NoFlag(0),
    Underline(1 shl 3),
    HighLight(1 shl 4),
    DontCommit(1 shl 5),
    Bold(1 shl 6),
    Strike(1 shl 7),
    Italic(1 shl 8)
}

fun Int.hasFlag(flag: TextFormatFlag) = hasFlag(flag.flag)

data class FormattedText(
    val strings: Array<String>,
    val flags: IntArray,
    /**
     * cursor index counts by Java's String length
     */
    val cursor: Int
) {

    constructor() : this(arrayOf(), intArrayOf(), -1)

    companion object {
        @JvmStatic
        @Suppress("UNUSED") // called from JNI
        fun fromByteCursor(
            strings: Array<String>,
            flags: IntArray,
            byteCursor: Int
        ): FormattedText {
            val cursor = if (byteCursor <= 0) {
                byteCursor
            } else {
                StringBuilder().apply {
                    var byteSize = 0
                    strings.forEach {
                        val bytes = it.encodeToByteArray()
                        val total = byteSize + bytes.size
                        if (total < byteCursor) {
                            append(it)
                            byteSize = total
                        } else {
                            if (total == byteCursor) append(it)
                            else append(String(bytes.copyOfRange(0, byteCursor - byteSize)))
                            return@apply
                        }
                    }
                }.length
            }
            return FormattedText(strings, flags, cursor)
        }
    }

    val length: Int
        get() = strings.sumOf { it.length }

    override fun toString() = buildString { strings.forEach { append(it) } }

    fun isEmpty() = strings.all { it.isEmpty() }

    fun isNotEmpty() = strings.any { it.isNotEmpty() }

    fun toSpannedString(highlightColor: Int) = buildSpannedString {
        for (i in strings.indices) {
            val str = strings[i]
            val fmt = flags[i]
            val start = length
            append(str)
            if (fmt == TextFormatFlag.NoFlag.flag) continue
            val end = length
            if (fmt.hasFlag(TextFormatFlag.Underline)) {
                setSpan(UnderlineSpan(), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (fmt.hasFlag(TextFormatFlag.HighLight)) {
                setSpan(BackgroundColorSpan(highlightColor), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (fmt.hasFlag(TextFormatFlag.Bold)) {
                setSpan(StyleSpan(BOLD), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (fmt.hasFlag(TextFormatFlag.Strike)) {
                setSpan(StrikethroughSpan(), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (fmt.hasFlag(TextFormatFlag.Italic)) {
                setSpan(StyleSpan(ITALIC), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /**
     * Returns the number of Unicode code points until specified index of this FormattedText.
     */
    fun codePointCountUntil(endIndex: Int): Int {
        var count = 0
        var length = 0
        for (str in strings) {
            val total = length + str.length
            if (total < endIndex) {
                count += str.codePointCount(0, str.length)
                length = total
            } else {
                count += str.codePointCount(0, endIndex - length)
                break
            }
        }
        return count
    }

    /**
     * generated by Android Studio
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormattedText

        if (cursor != other.cursor) return false
        if (!flags.contentEquals(other.flags)) return false
        if (!strings.contentEquals(other.strings)) return false

        return true
    }

    /**
     * generated by Android Studio
     */
    override fun hashCode(): Int {
        var result = strings.contentHashCode()
        result = 31 * result + flags.contentHashCode()
        result = 31 * result + cursor
        return result
    }
}
