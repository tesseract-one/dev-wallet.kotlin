package one.tesseract.devwallet

import one.tesseract.devwallet.UI
import one.tesseract.devwallet.service.WalletSubstrateService
import one.tesseract.devwallet.service.WalletTestService
import one.tesseract.devwallet.settings.KeySettingsProvider
import one.tesseract.devwallet.settings.TestSettingsProvider
import one.tesseract.service.Tesseract

class Core(val ui: UI, val dataDir: String) {
    val tesseract: Tesseract
    init {
        val testService = WalletTestService(ui, testSettingsProvider())
        val substrateService = WalletSubstrateService(ui, keySettingsProvider())

        tesseract = Tesseract
            .default()
            .service(testService)
            .service(substrateService)
    }

    fun testSettingsProvider(): TestSettingsProvider = TestSettingsProvider()

    fun keySettingsProvider(): KeySettingsProvider = KeySettingsProvider()
}