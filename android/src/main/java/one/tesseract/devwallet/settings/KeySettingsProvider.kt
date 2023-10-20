package one.tesseract.devwallet.settings

import one.tesseract.devwallet.entity.KeySettings

class KeySettingsProvider {
    fun load(): KeySettings =
        KeySettings("lalala")
    fun save(settings: KeySettings) {
        TODO()
    }
}