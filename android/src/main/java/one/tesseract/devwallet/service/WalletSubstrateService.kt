package one.tesseract.devwallet.service

import dev.sublab.sr25519.KeyPair

import one.tesseract.exception.UserCancelledException
import one.tesseract.protocol.common.substrate.AccountType
import one.tesseract.protocol.common.substrate.GetAccountResponse
import one.tesseract.protocol.kotlin.SubstrateService

import one.tesseract.devwallet.Application
import one.tesseract.devwallet.entity.request.SubstrateAccount
import one.tesseract.devwallet.entity.request.SubstrateSign
import one.tesseract.devwallet.substrate.Transaction
import one.tesseract.devwallet.substrate.address
import one.tesseract.devwallet.substrate.fromMnemonic
import one.tesseract.devwallet.settings.KeySettingsProvider

class WalletSubstrateService(private val application: Application, private val settings: KeySettingsProvider): SubstrateService {
    override suspend fun getAccount(type: AccountType): GetAccountResponse {
        val kp = KeyPair.fromMnemonic(settings.load().mnemonic)
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
        val transaction = Transaction(extrinsicData, extrinsicTypes, extrinsicMetadata)
        val transactionString = transaction.toString()

        val kp = KeyPair.fromMnemonic(settings.load().mnemonic)
        val address = kp.address()

        val signRequest = SubstrateSign(accountType.name, accountPath, address, transactionString)

        return if(application.requestUserConfirmation(signRequest)) {
            transaction.sign(kp.secretKey).toByteArray()
        } else {
            throw UserCancelledException()
        }
    }
}