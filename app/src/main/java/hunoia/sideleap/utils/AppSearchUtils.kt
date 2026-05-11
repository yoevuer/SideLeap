package hunoia.sideleap.utils

import android.content.Context
import net.sourceforge.pinyin4j.BasePinyinHelper
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.QuickAppLauncherSettings

internal data class AppSearchIndex(val raw: String, val pinyin: String, val initials: String)

internal fun sortApps(context: Context, apps: List<AppInfo>, settings: QuickAppLauncherSettings, tokens: List<String>): List<AppInfo> {
    val matched = if (tokens.isEmpty()) apps else apps.filter { app -> matchesApp(context, app, tokens) }
    return matched.sortedWith(compareByDescending<AppInfo> { prefixRank(context, it, tokens) }
        .thenByDescending { settings.recentLaunchTime[it.key()] ?: 0L }
        .thenByDescending { settings.launchCount[it.key()] ?: 0L }
        .thenByDescending { containsRank(context, it, tokens) }
        .thenBy { it.label.lowercase() })
}

internal fun matchesApp(context: Context, app: AppInfo, tokens: List<String>): Boolean {
    val index = buildIndex(context, app.label)
    return matchesTokens(index.initials, tokens) || matchesTokens(index.pinyin, tokens) || matchesTokens(index.raw, tokens)
}

internal fun prefixRank(context: Context, app: AppInfo, tokens: List<String>): Int {
    if (tokens.isEmpty()) return 0
    val index = buildIndex(context, app.label)
    return when {
        matchesTokens(index.initials, tokens) && index.initials.startsWith(tokens.joinToString("")) -> 2
        matchesTokens(index.raw, tokens) && index.raw.startsWith(tokens.joinToString("")) -> 1
        matchesTokens(index.pinyin, tokens) && index.pinyin.startsWith(tokens.joinToString("")) -> 1
        else -> 0
    }
}

internal fun containsRank(context: Context, app: AppInfo, tokens: List<String>): Int {
    if (tokens.isEmpty()) return 0
    val index = buildIndex(context, app.label)
    return if (matchesTokens(index.initials, tokens) || matchesTokens(index.pinyin, tokens) || matchesTokens(index.raw, tokens)) 1 else 0
}

internal fun buildIndex(context: Context, text: String): AppSearchIndex {
    val raw = text.lowercase()
    val pinyin = StringBuilder()
    val initials = StringBuilder()
    text.forEach { ch ->
        val py = runCatching { BasePinyinHelper.toHanyuPinyinStringArray(context, ch)?.firstOrNull() }.getOrNull()?.takeIf { it.isNotBlank() }
        if (py != null) {
            val plain = py.filter(Char::isLetter).lowercase()
            if (plain.isNotBlank()) { pinyin.append(plain); initials.append(plain.first()) }
        } else if (ch.isLetterOrDigit()) {
            val lower = ch.lowercaseChar(); pinyin.append(lower); initials.append(lower)
        }
    }
    return AppSearchIndex(raw, pinyin.toString(), initials.toString())
}

internal fun matchesTokens(text: String, tokens: List<String>): Boolean {
    if (tokens.isEmpty()) return true
    if (tokens.size > text.length) return false
    for (start in 0..(text.length - tokens.size)) {
        var ok = true
        for (i in tokens.indices) {
            val token = tokens[i]
            val ch = text[start + i]
            if (token.length == 1 && token[0].isDigit()) {
                if (token[0] != ch) { ok = false; break }
            } else if (!token.lowercase().contains(ch.lowercaseChar())) { ok = false; break }
        }
        if (ok) return true
    }
    return false
}

internal fun AppInfo.key() = "$packageName/$className"