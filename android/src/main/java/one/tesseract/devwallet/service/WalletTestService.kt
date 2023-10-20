package one.tesseract.devwallet.service

import one.tesseract.devwallet.UI
import one.tesseract.devwallet.entity.request.TestError
import one.tesseract.devwallet.entity.request.TestSign
import one.tesseract.devwallet.settings.TestSettingsProvider
import one.tesseract.exception.UserCancelledException
import one.tesseract.service.protocol.kotlin.TestService

class WalletTestService(val ui: UI, val settings: TestSettingsProvider): TestService {
    override suspend fun signTransaction(transaction: String): String {
        val settings = settings.load()

        val invalidator = settings.invalidator

        return if(invalidator == transaction) {
            val error = "Intentional error. Because your transaction `$transaction` is set as the invalidator in DevWallet Kotlin settings"
            val request = TestError(transaction, error)

            if(ui.requestUserConfirmation(request)) {
                throw Exception("Intentional exception: transaction = invalidator = `$transaction`")
            } else {
                throw UserCancelledException()
            }
        } else {
            val signature = settings.signature
            val signed = transaction + signature

            val request = TestSign(transaction, signature, signed)
            if (ui.requestUserConfirmation(request)) {
                signed
            } else {
                throw UserCancelledException()
            }
        }
    }
}