package one.tesseract.devwallet.settings

import android.content.SharedPreferences

import one.tesseract.devwallet.entity.KeySettings

private const val MNEMONIC = "mnemonic"

fun KeySettings.Companion.load(preferences: SharedPreferences): KeySettings {
    val mnemonic = preferences.getString(MNEMONIC, "")!!

    return KeySettings(mnemonic)
}

fun KeySettings.save(preferences: SharedPreferences) = preferences
    .edit()
    .putString(MNEMONIC, this.mnemonic) //don't do this in a wallet that is made to keep assets
    .apply()