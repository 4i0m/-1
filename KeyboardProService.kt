package com.keyboardpro.keyboard_pro

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class KeyboardProService : InputMethodService(), KeyboardListener {

    private lateinit var root: FrameLayout
    private lateinit var bgView: ImageView
    private lateinit var frost: View
    private lateinit var keyboard: GlassKeyboardView
    private lateinit var emojiPanel: LinearLayout
    private lateinit var emojiBack: TextView
    private lateinit var emojiDel: TextView

    private var bgCacheKey = ""
    private val dens get() = resources.displayMetrics.density

    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        root = FrameLayout(this)
        bgView = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        frost = View(this)
        keyboard = GlassKeyboardView(this).also { it.listener = this }
        emojiPanel = buildEmojiPanel()

        root.addView(bgView, FrameLayout.LayoutParams(MATCH, MATCH))
        root.addView(frost, FrameLayout.LayoutParams(MATCH, MATCH))
        root.addView(keyboard, FrameLayout.LayoutParams(MATCH, WRAP))
        root.addView(emojiPanel, FrameLayout.LayoutParams(MATCH, MATCH))
        applyPrefs()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        applyPrefs()
        keyboard.reset()
        showEmoji(false)
    }

    // ------------------------------------------------------------ settings

    private fun applyPrefs() {
        // Flutter's shared_preferences plugin stores everything in this file, keys prefixed "flutter."
        val p = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val id = try { p.getString("flutter.theme", "ios_auto") } catch (e: Exception) { null } ?: "ios_auto"
        val blur = try { p.getLong("flutter.blur", 12L).toInt() } catch (e: Exception) { 12 }
        val popups = try { p.getBoolean("flutter.blurPopups", true) } catch (e: Exception) { true }
        val haptics = try { p.getBoolean("flutter.haptics", true) } catch (e: Exception) { true }
        val image = try { p.getString("flutter.bgImage", null) } catch (e: Exception) { null }

        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val theme = KbThemes.byId(id, night)

        keyboard.kbTheme = theme
        keyboard.blurredPopups = popups
        keyboard.haptics = haptics
        frost.setBackgroundColor(theme.frost)
        emojiBack.setTextColor(theme.text)
        emojiDel.setTextColor(theme.text)
        updateBackground(theme, image, blur.coerceIn(0, 30))
        try { window.window?.navigationBarColor = theme.bgBottom or (0xFF shl 24) } catch (e: Exception) { }
    }

    private fun updateBackground(theme: KbTheme, imagePath: String?, blur: Int) {
        val file = imagePath?.let { File(it) }?.takeIf { it.exists() }
        val key = "${file?.path}|${file?.lastModified()}|$blur|${theme.bgTop}|${theme.bgBottom}"
        if (key == bgCacheKey) return
        bgCacheKey = key

        if (Build.VERSION.SDK_INT >= 31) bgView.setRenderEffect(null)
        bgView.scaleX = 1f
        bgView.scaleY = 1f

        if (file != null) {
            val bmp = decode(file, resources.displayMetrics.widthPixels)
            if (bmp != null) {
                if (blur > 0) {
                    val radius = blur * dens
                    if (Build.VERSION.SDK_INT >= 31) {
                        bgView.setImageBitmap(bmp)
                        bgView.setRenderEffect(
                            RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                        )
                    } else {
                        bgView.setImageBitmap(softBlur(bmp, radius))
                    }
                    bgView.scaleX = 1.08f // hides blur edge bleed
                    bgView.scaleY = 1.08f
                } else {
                    bgView.setImageBitmap(bmp)
                }
                return
            }
        }
        bgView.setImageDrawable(
            GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(theme.bgTop, theme.bgBottom))
        )
    }

    private fun decode(f: File, targetW: Int): Bitmap? = try {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, o)
        var s = 1
        while (o.outWidth / (s * 2) >= targetW) s *= 2
        BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = s })
    } catch (t: Throwable) {
        null
    }

    /** Pre-Android-12 fallback: cheap blur by down/up-scaling with bilinear filtering. */
    private fun softBlur(src: Bitmap, radius: Float): Bitmap {
        val scale = 1f / (1f + radius / 3f)
        val w = maxOf(1, (src.width * scale).toInt())
        val h = maxOf(1, (src.height * scale).toInt())
        val small = Bitmap.createScaledBitmap(src, w, h, true)
        return Bitmap.createScaledBitmap(small, src.width, src.height, true)
    }

    // ----------------------------------------------------------- emoji panel

    private val emojis = ("😀😃😄😁😆😅😂🤣😊😇🙂😉😍🥰😘😗😋😛😜🤪🤗🤔🤨😐😑😶🙄😏😴😌😔😪😷🤒🤕🥵🥶😎🤓🧐😕😟🙁😮😯😲😳🥺😢😭😱😖😞😓😩😫😤😡🤬😈💀💩🤡👻👽🤖" +
        "👍👎👌✌🤞🤟🤘👏🙌🙏💪👋🤝✍❤🧡💛💚💙💜🖤🤍💔💕💞💯🔥✨⭐🌙☀🌈🌹🌸🎉🎁🎂☕🍕🍔🍎🕌📿🕋").let { s ->
        val list = ArrayList<String>()
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            val n = Character.charCount(cp)
            list.add(s.substring(i, i + n))
            i += n
        }
        list
    }

    private fun buildEmojiPanel(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val grid = GridLayout(this).apply { columnCount = 8 }
        for (e in emojis) {
            val tv = TextView(this).apply {
                text = e
                textSize = 24f
                gravity = Gravity.CENTER
                setOnClickListener { onText(e) }
            }
            val lp = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f))
            lp.width = 0
            lp.height = (44 * dens).toInt()
            grid.addView(tv, lp)
        }
        val scroll = ScrollView(this).apply { addView(grid) }
        panel.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))

        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        emojiBack = TextView(this).apply {
            text = "أ ب ج"
            textSize = 16f
            gravity = Gravity.CENTER
            setOnClickListener { showEmoji(false) }
        }
        emojiDel = TextView(this).apply {
            text = "⌫"
            textSize = 20f
            gravity = Gravity.CENTER
            setOnClickListener { onBackspace() }
        }
        val h = (46 * dens).toInt()
        bar.addView(emojiBack, LinearLayout.LayoutParams(0, h, 1f))
        bar.addView(View(this), LinearLayout.LayoutParams(0, h, 3f))
        bar.addView(emojiDel, LinearLayout.LayoutParams(0, h, 1f))
        panel.addView(bar, LinearLayout.LayoutParams(MATCH, WRAP))
        return panel
    }

    private fun showEmoji(on: Boolean) {
        emojiPanel.visibility = if (on) View.VISIBLE else View.GONE
        keyboard.visibility = if (on) View.INVISIBLE else View.VISIBLE
    }

    // ------------------------------------------------------ KeyboardListener

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) ic.commitText("", 1)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
    }

    override fun onEnter() {
        val ic = currentInputConnection ?: return
        val opts = currentInputEditorInfo?.imeOptions ?: 0
        val action = opts and EditorInfo.IME_MASK_ACTION
        val noEnterAction = (opts and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (!noEnterAction && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    @Suppress("DEPRECATION")
    override fun onSwitchLanguage() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val switched = if (Build.VERSION.SDK_INT >= 28) {
            switchToNextInputMethod(false)
        } else {
            val token = window?.window?.attributes?.token
            token != null && imm.switchToNextInputMethod(token, false)
        }
        if (!switched) imm.showInputMethodPicker()
    }

    override fun onToggleEmoji() {
        showEmoji(emojiPanel.visibility != View.VISIBLE)
    }
}
