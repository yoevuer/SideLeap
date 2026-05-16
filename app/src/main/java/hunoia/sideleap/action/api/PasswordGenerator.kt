package hunoia.sideleap.action.api

import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordDefaultLength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordMaxLength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordMinLength
import hunoia.sideleap.settings.model.ActionSettings
import java.security.SecureRandom
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object PasswordGenerator {
    const val SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?"

    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val OTHER_CHAR_POOL_SIZE = 32
    private val secureRandom = SecureRandom()

    fun normalize(config: ActionSettings.PasswordGenerator): ActionSettings.PasswordGenerator {
        val hasAnyType = config.lowercase || config.uppercase || config.digits || config.symbols
        val enabledTypeCount = if (hasAnyType) enabledPools(config).size else 4
        val length = config.length
            .coerceIn(PasswordMinLength, PasswordMaxLength)
            .coerceAtLeast(enabledTypeCount)
        return if (hasAnyType) {
            config.copy(length = length)
        } else {
            ActionSettings.PasswordGenerator(length = PasswordDefaultLength.coerceAtLeast(enabledTypeCount))
        }
    }

    fun generate(config: ActionSettings.PasswordGenerator): String {
        val normalized = normalize(config)
        val pools = enabledPools(normalized)
        require(pools.isNotEmpty()) { "At least one character type is required" }

        val chars = mutableListOf<Char>()
        pools.forEach { pool -> chars += pool.randomChar() }
        val allChars = pools.joinToString(separator = "")
        repeat(normalized.length - chars.size) {
            chars += allChars.randomChar()
        }
        chars.shuffleSecure()
        return chars.joinToString(separator = "")
    }

    fun generatedEntropyBits(config: ActionSettings.PasswordGenerator): Int {
        val normalized = normalize(config)
        val poolSizes = enabledPools(normalized).map { it.length }
        if (poolSizes.isEmpty()) return 0
        val legalPasswordSpaceBits = constrainedEntropyBits(normalized.length, poolSizes)
        val generationProcessBits = generationProcessEntropyBits(normalized.length, poolSizes)
        return min(legalPasswordSpaceBits, generationProcessBits).roundToInt()
    }

    fun estimatedEntropyBits(text: String): Int {
        if (text.isEmpty()) return 0
        var poolSize = 0
        if (text.any { it in LOWERCASE }) poolSize += LOWERCASE.length
        if (text.any { it in UPPERCASE }) poolSize += UPPERCASE.length
        if (text.any { it in DIGITS }) poolSize += DIGITS.length
        if (text.any { it in SYMBOLS }) poolSize += SYMBOLS.length
        if (text.any { it !in LOWERCASE && it !in UPPERCASE && it !in DIGITS && it !in SYMBOLS }) {
            poolSize += OTHER_CHAR_POOL_SIZE
        }
        return (text.length * log2(poolSize.toDouble())).roundToInt()
    }

    fun enabledTypeCount(config: ActionSettings.PasswordGenerator): Int = enabledPools(config).size

    private fun enabledPools(config: ActionSettings.PasswordGenerator): List<String> = buildList {
        if (config.lowercase) add(LOWERCASE)
        if (config.uppercase) add(UPPERCASE)
        if (config.digits) add(DIGITS)
        if (config.symbols) add(SYMBOLS)
    }

    private fun constrainedEntropyBits(length: Int, poolSizes: List<Int>): Double {
        val totalPoolSize = poolSizes.sum()
        var validCount = 0.0
        val subsetCount = 1 shl poolSizes.size
        for (mask in 0 until subsetCount) {
            var excludedSize = 0
            for (index in poolSizes.indices) {
                if ((mask and (1 shl index)) != 0) {
                    excludedSize += poolSizes[index]
                }
            }
            val remainingSize = totalPoolSize - excludedSize
            if (remainingSize <= 0) continue
            val count = remainingSize.toDouble().pow(length)
            if (Integer.bitCount(mask) % 2 == 0) {
                validCount += count
            } else {
                validCount -= count
            }
        }
        return log2(max(validCount, 1.0))
    }

    private fun generationProcessEntropyBits(length: Int, poolSizes: List<Int>): Double {
        val requiredTypeCount = poolSizes.size
        val totalPoolSize = poolSizes.sum()
        val requiredCharBits = poolSizes.sumOf { log2(it.toDouble()) }
        val requiredPositionBits = log2Permutation(length, requiredTypeCount)
        val remainingCharBits = (length - requiredTypeCount) * log2(totalPoolSize.toDouble())
        return requiredPositionBits + requiredCharBits + remainingCharBits
    }

    private fun log2Permutation(n: Int, k: Int): Double {
        var result = 0.0
        repeat(k) { index ->
            result += log2((n - index).toDouble())
        }
        return result
    }

    private fun String.randomChar(): Char = this[secureRandom.nextInt(length)]

    private fun MutableList<Char>.shuffleSecure() {
        for (i in lastIndex downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = this[i]
            this[i] = this[j]
            this[j] = tmp
        }
    }
}
