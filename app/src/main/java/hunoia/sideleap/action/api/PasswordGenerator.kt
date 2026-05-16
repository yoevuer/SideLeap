package hunoia.sideleap.action.api

import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordDefaultLength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordMaxLength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordMinLength
import hunoia.sideleap.settings.model.ActionSettings
import java.security.SecureRandom
import kotlin.math.log2
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
        val poolSize = enabledPools(normalized).sumOf { it.length }
        if (poolSize <= 0) return 0
        return (normalized.length * log2(poolSize.toDouble())).roundToInt()
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
