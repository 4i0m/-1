package com.keyboardpro.keyboard_pro

import android.graphics.RectF

enum class KType { CHAR, BACKSPACE, ENTER, SPACE, SYMBOLS, LETTERS, EMOJI, LANG, DIAC }

class KKey(
    val label: String,
    val type: KType = KType.CHAR,
    val weight: Float = 1f,
    val alts: List<String> = emptyList()
) {
    val cell = RectF()
    var pressed = false
}

object KbLayouts {
    // fatha, damma, kasra, shadda, sukun, then tanween
    val diacritics = listOf(
        "\u064E", "\u064F", "\u0650", "\u0651", "\u0652", "\u064B", "\u064C", "\u064D"
    )
    private val digits = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠")
    private val variants = mapOf(
        "ا" to listOf("أ", "إ", "آ", "ٱ"),
        "و" to listOf("ؤ"),
        "ي" to listOf("ئ", "ى"),
        "ى" to listOf("ي"),
        "ه" to listOf("ة"),
        "ة" to listOf("ه"),
        "ء" to listOf("أ", "إ"),
        "ك" to listOf("گ"),
        "ف" to listOf("ڤ"),
        "ج" to listOf("چ"),
        "ب" to listOf("پ")
    )

    /** Combining marks are shown on a tatweel so they render visibly. */
    fun display(s: String): String =
        if (s.length == 1 && s[0] in '\u064B'..'\u0652') "\u0640$s" else s

    private fun alts(ch: String, digit: String? = null): List<String> =
        listOfNotNull(digit) + (variants[ch] ?: emptyList()) + diacritics.take(6)

    private fun bottom(first: KKey): List<KKey> = listOf(
        first,
        KKey("🙂", KType.EMOJI, 1.2f),
        KKey("‹ العربية ›", KType.LANG, 1.7f),
        KKey("كيبورد برو", KType.SPACE, 4.9f),
        KKey(".", alts = listOf("،", "؟", "!", "؛", ":")),
        KKey("↵", KType.ENTER, 1.6f)
    )

    // Rows are listed in logical order: first element = right-most key (RTL).
    fun letters(): List<List<KKey>> {
        val r1 = "ضصثقفغعهخحجد".mapIndexed { i, c ->
            KKey(c.toString(), alts = alts(c.toString(), digits.getOrNull(i)))
        }
        val r2 = "شسيبلاتنمكطذ".map { KKey(it.toString(), alts = alts(it.toString())) }
        val r3 = listOf(KKey("\u0640\u064E", KType.DIAC)) +
            listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ").map { KKey(it, alts = alts(it)) } +
            KKey("⌫", KType.BACKSPACE)
        return listOf(r1, r2, r3, bottom(KKey("?123", KType.SYMBOLS, 1.6f)))
    }

    fun symbols(): List<List<KKey>> {
        val s1 = "١٢٣٤٥٦٧٨٩٠+=".map { KKey(it.toString()) }
        val s2 = listOf("-", "/", ":", "؛", "(", ")", "﷼", "&", "@", "\"", "#", "*").map { KKey(it) }
        val s3 = listOf(KKey("«")) +
            listOf("،", ".", "؟", "!", "'", "…", "%", "\$", "_", "~").map { KKey(it) } +
            KKey("⌫", KType.BACKSPACE)
        return listOf(s1, s2, s3, bottom(KKey("أ ب ج", KType.LETTERS, 1.6f)))
    }
}
