package one.tesseract.devwallet.substrate

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.DEFAULT_PASSPHRASE
import cash.z.ecc.android.bip39.Mnemonics.INTERATION_COUNT
import cash.z.ecc.android.bip39.Mnemonics.KEY_SIZE

import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter

import dev.sublab.sr25519.ExpansionMode
import dev.sublab.sr25519.KeyPair
import dev.sublab.sr25519.MiniSecretKey
import dev.sublab.sr25519.PublicKey
import dev.sublab.ss58.ss58

private fun Mnemonics.MnemonicCode.toSubstrateSeed(passphrase: String): ByteArray =
    PKCS5S2ParametersGenerator(SHA512Digest()).run {
        init(this@toSubstrateSeed.toEntropy(),
            (DEFAULT_PASSPHRASE + passphrase).toByteArray(),
            INTERATION_COUNT)

        val keyParameter = generateDerivedMacParameters(KEY_SIZE) as KeyParameter
        keyParameter.key
    }

fun KeyPair.Companion.fromMnemonic(mnemonic: Mnemonics.MnemonicCode, passphrase: String): KeyPair {
    val seed = mnemonic.toSubstrateSeed(passphrase).copyOf(32)
    return MiniSecretKey.fromByteArray(seed).expand(ExpansionMode.ED25519).toKeyPair()
}

fun KeyPair.Companion.fromMnemonic(mnemonic: String): KeyPair =
    fromMnemonic(Mnemonics.MnemonicCode(mnemonic), "")

fun KeyPair.address(netId: Int = 42): String = publicKey.address(netId)
fun PublicKey.address(netId: Int = 42): String = this.toByteArray().ss58.address(netId)
