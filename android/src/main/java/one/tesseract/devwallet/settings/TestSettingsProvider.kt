package one.tesseract.devwallet.settings

import one.tesseract.devwallet.entity.TestSettings

class TestSettingsProvider {
    fun load(): TestSettings =
        TestSettings("signed_by_kotlin", "error_by_kotlin")
    fun save(settings: TestSettings) {
        TODO()
    }
}
