package com.keyboardpro.keyboard_pro

data class KbTheme(
    val bgTop: Int,
    val bgBottom: Int,
    val key: Int,
    val special: Int,
    val pressed: Int,
    val text: Int,
    val border: Int,
    val accent: Int,
    val accentText: Int,
    val frost: Int,
    val popupBase: Int,
    val popupText: Int
)

object KbThemes {
    private fun c(v: Long): Int = v.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()

    private fun glass(top: Long, bottom: Long, accent: Long, popup: Long) = KbTheme(
        bgTop = c(top), bgBottom = c(bottom),
        key = c(0x38FFFFFF), special = c(0x24FFFFFF), pressed = c(0x70FFFFFF),
        text = WHITE, border = c(0x55FFFFFF),
        accent = c(accent), accentText = WHITE,
        frost = c(0x14FFFFFF), popupBase = c(popup), popupText = WHITE
    )

    private val IOS_LIGHT = KbTheme(
        bgTop = c(0xFFD3D6DD), bgBottom = c(0xFFC4C8D0),
        key = c(0xE6FFFFFF), special = c(0x99B0B5C0), pressed = c(0xFFB9BDC7),
        text = c(0xFF111111), border = c(0x66FFFFFF),
        accent = c(0xFF007AFF), accentText = WHITE,
        frost = c(0x22FFFFFF), popupBase = c(0xFFFFFFFF), popupText = c(0xFF111111)
    )

    private val IOS_DARK = KbTheme(
        bgTop = c(0xFF2A2A2D), bgBottom = c(0xFF1B1B1D),
        key = c(0x55FFFFFF), special = c(0x33FFFFFF), pressed = c(0x88FFFFFF),
        text = WHITE, border = c(0x22FFFFFF),
        accent = c(0xFF0A84FF), accentText = WHITE,
        frost = c(0x14000000), popupBase = c(0xFF4A4A4D), popupText = WHITE
    )

    private val OLED = KbTheme(
        bgTop = c(0xFF000000), bgBottom = c(0xFF000000),
        key = c(0x1FFFFFFF), special = c(0x14FFFFFF), pressed = c(0x40FFFFFF),
        text = c(0xFFEDEDED), border = c(0x1AFFFFFF),
        accent = c(0xFF0A84FF), accentText = WHITE,
        frost = c(0x00000000), popupBase = c(0xFF1C1C1E), popupText = WHITE
    )

    fun byId(id: String, night: Boolean): KbTheme = when (id) {
        "ios_light" -> IOS_LIGHT
        "ios_dark" -> IOS_DARK
        "ios_auto" -> if (night) IOS_DARK else IOS_LIGHT
        "aura" -> glass(0xFFFF6FD8, 0xFF3813C2, 0xFFFF4D9D, 0xFF5B2A9E)
        "violet" -> glass(0xFF8B5CF6, 0xFF4C1D95, 0xFFC084FC, 0xFF5B21B6)
        "turquoise" -> glass(0xFF2DD4BF, 0xFF0F766E, 0xFF0F766E, 0xFF0F766E)
        "emerald" -> glass(0xFF34D399, 0xFF065F46, 0xFF059669, 0xFF047857)
        "amber" -> glass(0xFFFBBF24, 0xFFEA580C, 0xFFC2410C, 0xFFB45309)
        "oled" -> OLED
        else -> if (night) IOS_DARK else IOS_LIGHT
    }
}
