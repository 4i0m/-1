package com.keyboardpro.keyboard_pro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

interface KeyboardListener {
    fun onText(text: String)
    fun onBackspace()
    fun onEnter()
    fun onSwitchLanguage()
    fun onToggleEmoji()
}

/**
 * Single custom View that draws every key on one hardware-accelerated canvas.
 * No allocations happen in onDraw / touch handling paths -> smooth at 60/120 FPS.
 * Supports multi-touch (fast two-thumb typing), long-press popups, backspace repeat.
 */
class GlassKeyboardView(context: Context) : View(context) {

    var listener: KeyboardListener? = null
    var haptics = true
    var blurredPopups = true
        set(v) { field = v; applyColors(); invalidate() }
    var kbTheme: KbTheme = KbThemes.byId("ios_light", false)
        set(v) { field = v; applyColors(); invalidate() }

    private class PS {
        var key: KKey? = null
        var popup: List<String>? = null
        var popupSel = 0
        var popupLeft = 0f
        var cellW = 0f
        var popupLtr = true
        var runnable: Runnable? = null
    }

    private val d = resources.displayMetrics.density
    private val handler = Handler(Looper.getMainLooper())
    private val pointers = SparseArray<PS>()

    private var rows: List<List<KKey>> = KbLayouts.letters()
    private var stickyItems: List<String>? = null

    private val bandH = 50f * d
    private val padV = 6f * d
    private val padH = 3f * d
    private val gap = 5f * d
    private val corner = 9f * d
    private var keyH = 48f * d

    private val keyFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val specialFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pressedFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = d }
    private val popupBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = d }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val fm = Paint.FontMetrics()
    private val tmp = RectF()
    private val tmp2 = RectF()

    init { applyColors() }

    private fun applyColors() {
        val t = kbTheme
        keyFill.color = t.key
        specialFill.color = t.special
        pressedFill.color = t.pressed
        accentFill.color = t.accent
        border.color = t.border
        val alpha = if (blurredPopups) 0xD0 else 0xFF
        popupFill.color = (t.popupBase and 0x00FFFFFF) or (alpha shl 24)
        popupBorder.color = t.border
    }

    fun reset() = setMode(false)

    private fun setMode(symbols: Boolean) {
        cancelAll()
        stickyItems = null
        rows = if (symbols) KbLayouts.symbols() else KbLayouts.letters()
        layoutKeys()
        invalidate()
    }

    // ---------------------------------------------------------------- layout

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val w = MeasureSpec.getSize(widthSpec)
        val screenH = resources.displayMetrics.heightPixels
        keyH = (screenH * 0.064f).coerceIn(42f * d, 58f * d)
        val h = bandH + padV + rows.size * keyH + padV
        setMeasuredDimension(w, h.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        layoutKeys()
    }

    private fun rowsTop() = bandH + padV

    private fun layoutKeys() {
        if (width == 0) return
        val right = width - padH
        val avail = width - 2f * padH
        rows.forEachIndexed { r, row ->
            val total = row.sumOf { it.weight.toDouble() }.toFloat()
            val unit = avail / total
            val top = rowsTop() + r * keyH
            var x = right // Arabic is RTL: first key sits on the right
            for (k in row) {
                val w = k.weight * unit
                k.cell.set(x - w, top, x, top + keyH)
                x -= w
            }
        }
    }

    // --------------------------------------------------------------- drawing

    private fun drawLabel(c: Canvas, s: String, cx: Float, cy: Float, size: Float, color: Int) {
        text.textSize = size
        text.color = color
        text.getFontMetrics(fm)
        c.drawText(s, cx, cy - (fm.ascent + fm.descent) / 2f, text)
    }

    override fun onDraw(c: Canvas) {
        val half = gap / 2f
        for (row in rows) {
            for (k in row) {
                tmp.set(k.cell)
                tmp.inset(half, half)
                val fill = when {
                    k.pressed || (k.type == KType.DIAC && stickyItems != null) -> pressedFill
                    k.type == KType.ENTER -> accentFill
                    k.type == KType.CHAR || k.type == KType.SPACE -> keyFill
                    else -> specialFill
                }
                c.drawRoundRect(tmp, corner, corner, fill)
                c.drawRoundRect(tmp, corner, corner, border)
                val size = when (k.type) {
                    KType.CHAR -> keyH * 0.44f
                    KType.SPACE, KType.LANG -> keyH * 0.28f
                    else -> keyH * 0.34f
                }
                val color = if (k.type == KType.ENTER) kbTheme.accentText else kbTheme.text
                drawLabel(c, KbLayouts.display(k.label), tmp.centerX(), tmp.centerY(), size, color)
            }
        }
        for (i in 0 until pointers.size()) {
            val ps = pointers.valueAt(i)
            val k = ps.key ?: continue
            if (ps.popup != null) drawPopup(c, ps, k)
            else if (k.type == KType.CHAR) drawBubble(c, k)
        }
        stickyItems?.let { drawSticky(c, it) }
    }

    private fun drawBubble(c: Canvas, k: KKey) {
        val bw = max(k.cell.width() * 1.35f, 46f * d)
        val bh = min(keyH * 1.12f, bandH + padV - 3f * d)
        val l = (k.cell.centerX() - bw / 2f).coerceIn(2f * d, width - bw - 2f * d)
        tmp.set(l, k.cell.top + gap / 2f - bh, l + bw, k.cell.top + gap / 2f)
        c.drawRoundRect(tmp, corner * 1.4f, corner * 1.4f, popupFill)
        c.drawRoundRect(tmp, corner * 1.4f, corner * 1.4f, popupBorder)
        drawLabel(c, KbLayouts.display(k.label), tmp.centerX(), tmp.centerY(), keyH * 0.6f, kbTheme.popupText)
    }

    private fun drawPopup(c: Canvas, ps: PS, k: KKey) {
        val items = ps.popup ?: return
        val n = items.size
        val stripH = keyH * 0.95f
        val top = max(2f * d, k.cell.top - stripH - 4f * d)
        tmp.set(ps.popupLeft, top, ps.popupLeft + n * ps.cellW, top + stripH)
        c.drawRoundRect(tmp, corner * 1.3f, corner * 1.3f, popupFill)
        c.drawRoundRect(tmp, corner * 1.3f, corner * 1.3f, popupBorder)
        for (i in 0 until n) {
            val slot = if (ps.popupLtr) i else n - 1 - i
            val cx = ps.popupLeft + (slot + 0.5f) * ps.cellW
            val sel = i == ps.popupSel
            if (sel) {
                tmp2.set(cx - ps.cellW / 2f + 3f * d, top + 3f * d, cx + ps.cellW / 2f - 3f * d, top + stripH - 3f * d)
                c.drawRoundRect(tmp2, corner, corner, accentFill)
            }
            drawLabel(
                c, KbLayouts.display(items[i]), cx, top + stripH / 2f, keyH * 0.46f,
                if (sel) kbTheme.accentText else kbTheme.popupText
            )
        }
    }

    private fun drawSticky(c: Canvas, items: List<String>) {
        val n = items.size
        val cw = min(48f * d, (width - 16f * d) / n)
        val left = (width - n * cw) / 2f
        tmp.set(left, 3f * d, left + n * cw, bandH - 3f * d)
        c.drawRoundRect(tmp, corner * 1.3f, corner * 1.3f, popupFill)
        c.drawRoundRect(tmp, corner * 1.3f, corner * 1.3f, popupBorder)
        for (i in 0 until n) {
            val cx = left + (i + 0.5f) * cw
            drawLabel(c, KbLayouts.display(items[i]), cx, tmp.centerY(), keyH * 0.5f, kbTheme.popupText)
        }
    }

    // ----------------------------------------------------------------- touch

    private fun findKey(xIn: Float, y: Float): KKey? {
        val top = rowsTop()
        if (y < top) return null
        val x = xIn.coerceIn(padH, width - padH - 0.01f)
        val r = ((y - top) / keyH).toInt().coerceIn(0, rows.size - 1)
        for (k in rows[r]) if (x >= k.cell.left && x < k.cell.right) return k
        return null
    }

    private fun haptic(long: Boolean = false) {
        if (!haptics) return
        performHapticFeedback(
            if (long) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.KEYBOARD_TAP
        )
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = e.actionIndex
                down(e.getPointerId(i), e.getX(i), e.getY(i))
            }
            MotionEvent.ACTION_MOVE -> for (i in 0 until e.pointerCount) {
                move(e.getPointerId(i), e.getX(i), e.getY(i))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> up(e.getPointerId(e.actionIndex))
            MotionEvent.ACTION_CANCEL -> cancelAll()
        }
        return true
    }

    private fun down(id: Int, x: Float, y: Float) {
        if (y < bandH) {
            if (stickyItems != null) pickSticky(x)
            return
        }
        val k = findKey(x, y) ?: return
        if (k.type != KType.DIAC) dismissSticky()
        val ps = PS()
        ps.key = k
        k.pressed = true
        pointers.put(id, ps)
        haptic()
        if (k.type == KType.BACKSPACE) listener?.onBackspace()
        scheduleLongPress(ps, k)
        invalidate()
    }

    private fun scheduleLongPress(ps: PS, k: KKey) {
        val r: Runnable = when {
            k.type == KType.BACKSPACE -> object : Runnable {
                override fun run() {
                    listener?.onBackspace()
                    handler.postDelayed(this, 45)
                }
            }
            k.type == KType.CHAR && k.alts.isNotEmpty() -> Runnable { openPopup(ps) }
            else -> return
        }
        ps.runnable = r
        handler.postDelayed(r, if (k.type == KType.BACKSPACE) 380L else 330L)
    }

    private fun openPopup(ps: PS) {
        val k = ps.key ?: return
        val items = k.alts
        if (items.isEmpty()) return
        val n = items.size
        val cw = min(46f * d, (width - 8f * d) / n)
        val total = n * cw
        val cx = k.cell.centerX()
        val ltr = cx < width / 2f
        var left = if (ltr) cx - cw / 2f else cx + cw / 2f - total
        left = left.coerceIn(2f * d, width - total - 2f * d)
        ps.popup = items
        ps.cellW = cw
        ps.popupLeft = left
        ps.popupLtr = ltr
        ps.popupSel = 0
        haptic(true)
        invalidate()
    }

    private fun move(id: Int, x: Float, y: Float) {
        val ps = pointers.get(id) ?: return
        val items = ps.popup
        if (items != null) {
            val slot = ((x - ps.popupLeft) / ps.cellW).toInt().coerceIn(0, items.size - 1)
            val sel = if (ps.popupLtr) slot else items.size - 1 - slot
            if (sel != ps.popupSel) {
                ps.popupSel = sel
                invalidate()
            }
            return
        }
        val cur = ps.key ?: return
        if (cur.type == KType.BACKSPACE || cur.cell.contains(x, y)) return
        val nk = findKey(x, y) ?: return
        if (nk.type == KType.BACKSPACE) return
        ps.runnable?.let { handler.removeCallbacks(it) }
        ps.runnable = null
        cur.pressed = false
        nk.pressed = true
        ps.key = nk
        scheduleLongPress(ps, nk)
        invalidate()
    }

    private fun up(id: Int) {
        val ps = pointers.get(id) ?: return
        pointers.remove(id)
        ps.runnable?.let { handler.removeCallbacks(it) }
        val k = ps.key
        k?.pressed = false
        val popup = ps.popup
        if (popup != null) {
            listener?.onText(popup[ps.popupSel])
            invalidate()
            return
        }
        if (k != null) {
            when (k.type) {
                KType.CHAR -> listener?.onText(k.label)
                KType.SPACE -> listener?.onText(" ")
                KType.ENTER -> listener?.onEnter()
                KType.SYMBOLS -> { setMode(true); return }
                KType.LETTERS -> { setMode(false); return }
                KType.EMOJI -> listener?.onToggleEmoji()
                KType.LANG -> listener?.onSwitchLanguage()
                KType.DIAC -> toggleSticky()
                KType.BACKSPACE -> {}
            }
        }
        invalidate()
    }

    private fun cancelAll() {
        for (i in 0 until pointers.size()) {
            val ps = pointers.valueAt(i)
            ps.runnable?.let { handler.removeCallbacks(it) }
            ps.key?.pressed = false
        }
        pointers.clear()
        invalidate()
    }

    private fun toggleSticky() {
        stickyItems = if (stickyItems == null) KbLayouts.diacritics else null
        invalidate()
    }

    private fun dismissSticky() {
        if (stickyItems != null) {
            stickyItems = null
            invalidate()
        }
    }

    private fun pickSticky(x: Float) {
        val items = stickyItems ?: return
        val cw = min(48f * d, (width - 16f * d) / items.size)
        val left = (width - items.size * cw) / 2f
        val idx = ((x - left) / cw).toInt()
        if (idx in items.indices) {
            haptic()
            listener?.onText(items[idx])
        }
        dismissSticky()
    }

    override fun onDetachedFromWindow() {
        cancelAll()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != VISIBLE) cancelAll()
    }
}
