package one.tesseract.devwallet

import android.os.Parcelable

import one.tesseract.activity.detached.Launcher

import one.tesseract.service.Tesseract

import one.tesseract.devwallet.service.WalletSubstrateService
import one.tesseract.devwallet.service.WalletTestService
import one.tesseract.devwallet.settings.KeySettingsProvider
import one.tesseract.devwallet.settings.TestSettingsProvider
import one.tesseract.devwallet.ui.sign.SignActivity

class Application: android.app.Application() {
    private lateinit var tesseract: Tesseract
    private lateinit var launcher: Launcher

    override fun onCreate() {
        super.onCreate()

        launcher = Launcher(this)

        val testService = WalletTestService(this, testSettingsProvider())
        val substrateService = WalletSubstrateService(this, keySettingsProvider())

        tesseract = Tesseract
            .default()
            .service(testService)
            .service(substrateService)
    }

    suspend fun <T: Parcelable>requestUserConfirmation(request: T): Boolean {
        return SignActivity.requestUserConfirmation(launcher, request)
    }

    fun testSettingsProvider(): TestSettingsProvider = TestSettingsProvider(this)

    fun keySettingsProvider(): KeySettingsProvider = KeySettingsProvider(this)
}