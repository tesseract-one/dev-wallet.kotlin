package one.tesseract.devwallet.substrate

import dev.sublab.scale.ByteArrayReader
import dev.sublab.scale.ScaleCodec
import dev.sublab.sr25519.SecretKey
import dev.sublab.sr25519.Signature
import dev.sublab.sr25519.SigningContext
import one.tesseract.devwallet.substrate.metadata.RuntimeExtrinsic
import one.tesseract.devwallet.substrate.metadata.RuntimeLookup
import one.tesseract.devwallet.substrate.metadata.TypeId
import one.tesseract.devwallet.substrate.metadata.Value

class Transaction(private val tx: ByteArray, types: ByteArray, metadata: ByteArray) {
    private val call: Value<TypeId>
    private val extra: Array<Value<TypeId>>
    private val additional: Array<Value<TypeId>>

    init {
        val codec = ScaleCodec.default()
        val meta = codec.fromScale(metadata, RuntimeExtrinsic::class)
        @Suppress("NAME_SHADOWING") val types = codec.fromScale(types, RuntimeLookup::class)
        val reader = ByteArrayReader(tx)
        val call = Value.decode(reader, meta.type, types)
        val extra = meta.signedExtensions.map {
            Value.decode(reader, it.type, types)
        }
        val additional = meta.signedExtensions.map {
            Value.decode(reader, it.additionalSigned, types)
        }
        this.call = call
        this.extra = extra.toTypedArray()
        this.additional = additional.toTypedArray()
    }

    fun sign(secret: SecretKey): Signature {
        val context = SigningContext
            .fromContext("substrate".toByteArray())
            .bytes(tx)

        return secret.sign(context)
    }

    override fun toString(): String =
        "call: $call,\n\n" +
                "extra: ${extra.contentDeepToString()},\n\n" +
                "additional: ${additional.contentDeepToString()}"
}