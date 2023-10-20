package one.tesseract.devwallet.service.tmp

import dev.sublab.scale.ByteArrayReader
import dev.sublab.scale.ScaleCodec
import dev.sublab.sr25519.SecretKey
import dev.sublab.sr25519.Signature
import dev.sublab.sr25519.SigningContext

data class Transaction(val tx: ByteArray,
                       val call: Value<TypeId>,
                       val extra: Array<Value<TypeId>>,
                       val additional: Array<Value<TypeId>>)
{
    override fun toString(): String =
        "call: $call,\n\n"+
                "extra: ${extra.contentDeepToString()},\n\n"+
                "additional: ${additional.contentDeepToString()}"

    companion object {
        fun from(tx: ByteArray, types: ByteArray, metadata: ByteArray): Transaction {
            val codec = ScaleCodec.default()
            val meta = codec.fromScale(metadata, RuntimeExtrinsic::class)
            val types = codec.fromScale(types, RuntimeLookup::class)
            val reader = ByteArrayReader(tx)
            val call = Value.decode(reader, meta.type, types)
            val extra = meta.signedExtensions.map {
                Value.decode(reader, it.type, types)
            }
            val additional = meta.signedExtensions.map {
                Value.decode(reader, it.additionalSigned, types)
            }
            return Transaction(tx, call, extra.toTypedArray(), additional.toTypedArray())
        }
    }

    fun sign(secret: SecretKey): Signature {
        val context = SigningContext
            .fromContext("substrate".toByteArray())
            .bytes(tx)
        return secret.sign(context)
    }
}