package one.tesseract.devwallet.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

import one.tesseract.devwallet.entity.TestSettings

class TestSettingsProvider(context: Context) {
    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences("test_settings", MODE_PRIVATE)
    }
    fun load(): TestSettings = TestSettings.load(preferences)
    fun save(settings: TestSettings) = settings.save(preferences)
}
