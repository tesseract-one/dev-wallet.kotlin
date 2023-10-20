package one.tesseract.devwallet.settings

import android.content.SharedPreferences

import one.tesseract.devwallet.entity.TestSettings

private const val SIGNATURE = "signature"
private const val INVALIDATOR = "invalidator"

fun TestSettings.Companion.load(preferences: SharedPreferences): TestSettings {
    val signature = preferences.getString(SIGNATURE, "signed_by_tesseract")!!
    val invalidator = preferences.getString(INVALIDATOR, "err")!!

    return TestSettings(signature, invalidator)
}

fun TestSettings.save(preferences: SharedPreferences) = preferences
    .edit()
    .putString(SIGNATURE, this.signature)
    .putString(INVALIDATOR, this.invalidator)
    .apply()