package one.tesseract.devwallet.service

import cash.z.ecc.android.bip39.Mnemonics
import dev.sublab.sr25519.KeyPair
import one.tesseract.devwallet.Application
import one.tesseract.devwallet.entity.request.SubstrateAccount
import one.tesseract.devwallet.entity.request.SubstrateSign
import one.tesseract.devwallet.service.tmp.Transaction
import one.tesseract.devwallet.service.tmp.address
import one.tesseract.devwallet.service.tmp.fromMnemonic
import one.tesseract.devwallet.settings.KeySettingsProvider
import one.tesseract.exception.UserCancelledException
import one.tesseract.service.protocol.common.substrate.AccountType
import one.tesseract.service.protocol.common.substrate.GetAccountResponse
import one.tesseract.service.protocol.kotlin.SubstrateService

class WalletSubstrateService(val application: Application, val settings: KeySettingsProvider): SubstrateService {
    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun getAccount(type: AccountType): GetAccountResponse {
        val kp = KeyPair.fromMnemonic(Mnemonics.MnemonicCode("nuclear text arrow gloom magnet aisle butter chair raven seek desert census"), "")
        val address = kp.address()

        val accountRequest = SubstrateAccount(type.name, "", address)
        val allow = application.requestUserConfirmation(accountRequest)

        return if(allow) {
            GetAccountResponse(kp.publicKey.toByteArray(), "")
        } else {
            throw UserCancelledException()
        }
    }

    override suspend fun signTransaction(
        accountType: AccountType,
        accountPath: String,
        extrinsicData: ByteArray,
        extrinsicMetadata: ByteArray,
        extrinsicTypes: ByteArray
    ): ByteArray {
        val transaction = Transaction.from(extrinsicData, extrinsicTypes, extrinsicMetadata)
        val transactionString = transaction.toString()

        val kp = KeyPair.fromMnemonic(Mnemonics.MnemonicCode("nuclear text arrow gloom magnet aisle butter chair raven seek desert census"), "")
        val address = kp.address()

        val signRequest = SubstrateSign(accountType.name, accountPath, address, transactionString)

        val allow = application.requestUserConfirmation(signRequest)

        return if(allow) {
            transaction.sign(kp.secretKey).toByteArray()
        } else {
            throw UserCancelledException()
        }
    }
}