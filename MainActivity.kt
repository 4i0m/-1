package com.keyboardpro.keyboard_pro

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "keyboard_pro/ime")
            .setMethodCallHandler { call, result ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                when (call.method) {
                    "isEnabled" ->
                        result.success(imm.enabledInputMethodList.any { it.packageName == packageName })
                    "isSelected" -> {
                        val cur = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                        result.success(cur?.startsWith("$packageName/") == true)
                    }
                    "openSettings" -> {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        result.success(null)
                    }
                    "showPicker" -> {
                        imm.showInputMethodPicker()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }
}
