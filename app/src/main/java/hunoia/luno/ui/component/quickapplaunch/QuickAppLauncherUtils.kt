package hunoia.luno.ui.component.quickapplaunch

import com.github.promeg.pinyinhelper.Pinyin

internal enum class Page { App, Settings, Password }

internal fun pageMatchesTokens(pageName: String, tokens: List<String>): Boolean {
    val text = pageName.lowercase()
    val pinyin = buildString { pageName.forEach { append(Pinyin.toPinyin(it).lowercase()) } }
    val initials = buildString { pageName.forEach { append(Pinyin.toPinyin(it).first().lowercaseChar()) } }
    return matchesTokens(text, tokens) || matchesTokens(pinyin, tokens) || matchesTokens(initials, tokens)
}

internal fun matchesTokens(text: String, tokens: List<String>): Boolean {
    if (tokens.isEmpty()) return false
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
