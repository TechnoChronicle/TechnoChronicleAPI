package net.technochronicle.technochronicleapi.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents

import com.google.common.base.CaseFormat
import org.apache.commons.lang3.StringUtils

import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.math.log10
import kotlin.math.pow

object FormattingUtil {
    private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.ROOT)
    val DECIMAL_FORMAT_0F: DecimalFormat = DecimalFormat(",###")
    val DECIMAL_FORMAT_1F: DecimalFormat = DecimalFormat("#,##0.#")
    val DECIMAL_FORMAT_2F: DecimalFormat = DecimalFormat("#,##0.##")
    val DECIMAL_FORMAT_SIC: DecimalFormat = DecimalFormat("0E00")
    val DECIMAL_FORMAT_SIC_2F: DecimalFormat = DecimalFormat("0.00E00")

    private val SMALL_DOWN_NUMBER_BASE = '\u2080'.code
    private val SMALL_UP_NUMBER_BASE = '\u2070'.code
    private val SMALL_UP_NUMBER_ONE = '\u00B9'.code
    private val SMALL_UP_NUMBER_TWO = '\u00B2'.code
    private val SMALL_UP_NUMBER_THREE = '\u00B3'.code
    private val NUMBER_BASE = '0'.code

    fun toSmallUpNumbers(string: String): String = checkNumbers(string, SMALL_UP_NUMBER_BASE, true)

    fun toSmallDownNumbers(string: String): String = checkNumbers(string, SMALL_DOWN_NUMBER_BASE, false)

    private fun checkNumbers(string: String, smallUpNumberBase: Int, isUp: Boolean): String {
        val charArray = string.toCharArray()
        for (i in charArray.indices) {
            val relativeIndex = charArray[i].code - NUMBER_BASE
            if (relativeIndex >= 0 && relativeIndex <= 9) {
                if (isUp) {
                    if (relativeIndex == 1) {
                        charArray[i] = SMALL_UP_NUMBER_ONE.toChar()
                        continue
                    } else if (relativeIndex == 2) {
                        charArray[i] = SMALL_UP_NUMBER_TWO.toChar()
                        continue
                    } else if (relativeIndex == 3) {
                        charArray[i] = SMALL_UP_NUMBER_THREE.toChar()
                        continue
                    }
                }
                val newChar = smallUpNumberBase + relativeIndex
                charArray[i] = newChar.toChar()
            }
        }
        return String(charArray)
    }

    /**
     * Does almost the same thing as UPPER_CAMEL.to(LOWER_UNDERSCORE, string), but it also inserts underscores between
     * words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries:
     * "maragingSteel300" -> "maraging_steel_300"
     */
    fun toLowerCaseUnderscore(string: String): String {
        val result = StringBuilder()
        for (i in 0..<string.length) {
            if (i != 0 && (
                    Character.isUpperCase(string.get(i)) ||
                        (Character.isDigit(string.get(i - 1)) xor Character.isDigit(string.get(i)))
                    )
            ) {
                result.append("_")
            }
            result.append(string.get(i).lowercaseChar())
        }
        return result.toString()
    }

    /**
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word boundaries:
     * "maragingSteel300" -> "maraging_steel_300"
     */
    fun toLowerCaseUnder(string: String): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string)

    /**
     * apple_orange.juice => Apple Orange (Juice)
     */
    fun toEnglishName(internalName: String): String = Arrays.stream<String>(
        internalName.lowercase().split("_".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray(),
    )
        .map<String?> { str: String? -> StringUtils.capitalize(str) }
        .collect(Collectors.joining(" "))

    /**
     * Converts integers to roman numerals.
     * e.g. 17 => XVII, 2781 => MMDCCLXXXI
     */
    fun toRomanNumeral(number: Int): String = "I".repeat(number)
        .replace("IIIII", "V")
        .replace("IIII", "IV")
        .replace("VV", "X")
        .replace("VIV", "IX")
        .replace("XXXXX", "L")
        .replace("XXXX", "XL")
        .replace("LL", "C")
        .replace("LXL", "XC")
        .replace("CCCCC", "D")
        .replace("CCCC", "CD")
        .replace("DD", "M")
        .replace("DCD", "CM")

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before
     * numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries:
     * "maraging_steel_300" -> "maragingSteel300"
     */
    fun lowerUnderscoreToUpperCamel(string: String): String {
        val result = StringBuilder()
        for (i in 0..<string.length) {
            if (string.get(i) == '_') continue
            if (i == 0 || string.get(i - 1) == '_') {
                result.append(string.get(i).uppercaseChar())
            } else {
                result.append(string.get(i))
            }
        }
        return result.toString()
    }

    fun formatPercent(number: Double): String = String.format("%,.2f", number)

    /** To avoids (un)boxing.  */
    fun formatNumbers(number: Int): String = NUMBER_FORMAT.format(number.toLong())

    fun formatNumbers(number: Long): String = NUMBER_FORMAT.format(number)

    fun formatNumbers(number: Double): String = NUMBER_FORMAT.format(number)

    /** Allows for formatting Long, Integer, Short, Byte, Number, AtomicInteger, AtomicLong, and BigInteger.  */
    fun formatNumbers(number: Any?): String = NUMBER_FORMAT.format(number)

    @JvmOverloads
    fun formatNumberReadable(number: Long, milli: Boolean = false): String = formatNumberReadable(number.toDouble(), milli, DECIMAL_FORMAT_1F, null)

    fun formatNumberReadable2F(number: Double, milli: Boolean): String = formatNumberReadable(number, milli, DECIMAL_FORMAT_2F, null)

    /**
     * Format number in engineering notation with SI prefixes [m, k, M, G, T, P, E, Z]
     *
     * @param number Number to format
     * @param milli  Whether the passed number is already in millis (e.g., mB)
     * @param fmt    Formatter to use for compacted number
     * @param unit   Optional unit to append
     * @return Compacted number with SI prefix
     */
    fun formatNumberReadable(number: Double, milli: Boolean, fmt: NumberFormat, unit: String?): String {
        var number = number
        var milli = milli
        val sb = StringBuilder()
        if (number < 0) {
            number = -number
            sb.append('-')
        }

        if (milli && number >= 1e3) {
            milli = false
            number /= 1e3
        }

        var exp = 0
        if (number >= 1e3) {
            exp = log10(number).toInt() / 3
            if (exp > 7) exp = 7
            if (exp > 0) number /= 1e3.pow(exp.toDouble())
        }

        sb.append(fmt.format(number))
        if (exp > 0) {
            sb.append("kMGTPEZ".get(exp - 1))
        } else if (milli && number != 0.0) {
            sb.append('m')
        }

        if (unit != null) sb.append(unit)
        return sb.toString()
    }

    fun formatNumberOrSic(number: BigInteger, threshold: BigInteger?): String = if (number.compareTo(threshold) > 0) DECIMAL_FORMAT_SIC_2F.format(number) else formatNumbers(number)

    fun formatBuckets(mB: Long): String = formatNumberReadable(mB.toDouble(), true, DECIMAL_FORMAT_2F, "B")

    fun formatNumber2Places(number: Float): String = DECIMAL_FORMAT_2F.format(number.toDouble())

    fun formatNumber2Places(number: Double): String = DECIMAL_FORMAT_2F.format(number)

    fun formatPercentage2Places(langKey: String, percentage: Float): Component = Component.translatable(langKey, formatNumber2Places(percentage)).withStyle(ChatFormatting.YELLOW)

    fun combineComponents(c1: MutableComponent, c2: Component) {
        if (!isEmptyComponent(c1) && !isEmptyComponent(c2)) {
            c1.append(", ").append(c2)
        } else {
            c1.append(c2)
        }
    }

    private fun isEmptyComponent(component: Component): Boolean = component.getContents() === PlainTextContents.EMPTY && component.getSiblings().isEmpty()
}
