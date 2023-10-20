package one.tesseract.devwallet.settings

import android.content.Context
import android.content.SharedPreferences

import one.tesseract.devwallet.entity.KeySettings

class KeySettingsProvider(context: Context) {
    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences("key_settings", Context.MODE_PRIVATE)
    }

    fun load(): KeySettings = KeySettings.load(preferences)
    fun save(settings: KeySettings) = settings.save(preferences)
}