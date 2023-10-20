package one.tesseract.devwallet.substrate.metadata

import java.math.BigInteger

import kotlin.reflect.typeOf

import dev.sublab.scale.ByteArrayReader
import dev.sublab.common.numerics.*
import dev.sublab.scale.adapters.*
import dev.sublab.scale.ScaleCodecAdapterProvider
import dev.sublab.scale.default.DefaultScaleCodecAdapterProvider
import dev.sublab.scale.read
import dev.sublab.hex.hex

import one.tesseract.devwallet.substrate.metadata.RuntimeTypeDefPrimitive.*

sealed interface ValueContext {
}

sealed interface ValueDefinition<C: ValueContext> {
    data class Map<C : ValueContext>(
        val fields: kotlin.collections.Map<String, Value<C>>
    ) : ValueDefinition<C> {
        override fun toString(): String = fields.toString()
    }
    data class Sequence<C : ValueContext>(val sequence: Array<Value<C>>) : ValueDefinition<C> {
        override fun toString(): String = sequence.contentToString()
    }
    data class Variant<C : ValueContext>(val variant: VariantDefinition<C>) : ValueDefinition<C> {
        override fun toString(): String = variant.toString()
    }
    data class Primitive<C : ValueContext>(val primitive: PrimitiveDefinition) :
        ValueDefinition<C> {
        override fun toString(): String = primitive.toString()
    }
    //data class BitSequence<C : ValueContext>(val bitSeq: BooleanArray) : ValueDefinition<C>
}

sealed interface VariantDefinition<C: ValueContext> {
    val name: String

    data class Sequence<C: ValueContext>(override val name: String,
                                         val sequence: Array<Value<C>>): VariantDefinition<C>
    {
        override fun toString(): String = "<$name>${sequence.contentToString()}"
    }

    data class Map<C: ValueContext>(
        override val name: String, val fields: kotlin.collections.Map<String, Value<C>>
    ): VariantDefinition<C> {
        override fun toString(): String = "<$name>$fields"
    }
}

sealed interface PrimitiveDefinition {
    data class Bool(val bool: Boolean) : PrimitiveDefinition {
        override fun toString(): kotlin.String = bool.toString()
    }
    data class Bytes(val bytes: ByteArray) : PrimitiveDefinition {
        override fun toString(): kotlin.String = bytes.hex.encode(true)
    }
    data class String(val string: kotlin.String) : PrimitiveDefinition {
        override fun toString(): kotlin.String = string
    }
    data class Int(val int: BigInteger) : PrimitiveDefinition {
        override fun toString(): kotlin.String = int.toString()
    }
    data class Char(val char: UInt32) : PrimitiveDefinition {
        override fun toString(): kotlin.String = "U+%X".format(char)
    }
}

interface DynamicScaleDecodable<T> {
    fun decode(from: ByteArray, id: TypeId, types: RuntimeLookup,
               adapters: ScaleCodecAdapterProvider = DefaultScaleCodecAdapterProvider()): T {
        return decode(ByteArrayReader(from), id, types, adapters)
    }

    fun decode(from: ByteArrayReader, id: TypeId, types: RuntimeLookup,
               adapters: ScaleCodecAdapterProvider = DefaultScaleCodecAdapterProvider()): T {
        val type = types.findItemById(id)!!
        return decode(from, type, id, types, adapters)
    }

    fun decode(from: ByteArrayReader, type: RuntimeType,
               id: TypeId, types: RuntimeLookup,
               adapters: ScaleCodecAdapterProvider): T
}

data class Value<C: ValueContext>(val definition: ValueDefinition<C>, val context: C) {
    override fun toString(): String = definition.toString()

    companion object : DynamicScaleDecodable<Value<TypeId>> {
        override fun decode(
            from: ByteArrayReader, type: RuntimeType,
            id: TypeId, types: RuntimeLookup,
            adapters: ScaleCodecAdapterProvider
        ): Value<TypeId> = Value(
            when(val def = type.def) {
                is RuntimeTypeDef.Primitive -> decodePrimitive(def.primitive, from, adapters)
                is RuntimeTypeDef.Compact -> decodeCompact(from, adapters)
                is RuntimeTypeDef.BitSequence ->
                    throw InvalidTypeException(typeOf<RuntimeTypeDef.BitSequence>())
                is RuntimeTypeDef.Tuple -> decodeTuple(def.tuple, from, types, adapters)
                is RuntimeTypeDef.Array -> decodeArray(def.array, from, types, adapters)
                is RuntimeTypeDef.Sequence -> decodeSequence(def.sequence, from, types, adapters)
                is RuntimeTypeDef.Composite -> decodeComposite(def.composite, from, types, adapters)
                is RuntimeTypeDef.Variant -> decodeVariant(def.variant, from, types, adapters)
            }, id
        )

        private fun decodeVariant(
            type: RuntimeTypeDefVariant, from: ByteArrayReader,
            types: RuntimeLookup, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> {
            val index = from.readByte().toUInt()
            val variant = type.variants.find { it.index == index }!!
            val composite = decodeComposite(
                RuntimeTypeDefComposite(variant.fields), from, types, adapters
            )
            return ValueDefinition.Variant(
                when (composite) {
                    is ValueDefinition.Sequence ->
                        VariantDefinition.Sequence(variant.name, composite.sequence)

                    is ValueDefinition.Map ->
                        VariantDefinition.Map(variant.name, composite.fields)

                    else ->
                        throw InvalidTypeException(typeOf<ValueDefinition<TypeId>>())
                }
            )
        }

        private fun decodeComposite(
            type: RuntimeTypeDefComposite, from: ByteArrayReader,
            types: RuntimeLookup, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> {
            if (type.fields.isEmpty()) return ValueDefinition.Sequence(arrayOf())
            return if (type.fields[0].name == null) {
                ValueDefinition.Sequence(type.fields.map {
                    decode(from, it.type, types, adapters)
                }.toTypedArray())
            } else {
                ValueDefinition.Map(type.fields.associate {
                    it.name!! to decode(
                        from, it.type, types, adapters
                    )
                })
            }
        }

        private fun decodeSequence(
            type: RuntimeTypeDefSequence, from: ByteArrayReader,
            types: RuntimeLookup, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> {
            val chType = types.findItemById(type.type)!!
            val chDef = chType.def
            if (chDef is RuntimeTypeDef.Primitive && chDef.primitive == U8) {
                val bytes = adapters.findAdapter(ByteArray::class).read(from, ByteArray::class)
                return ValueDefinition.Primitive(PrimitiveDefinition.Bytes(bytes))
            }
            val length = adapters.findAdapter(BigInteger::class).read(from, BigInteger::class).toInt()
            val children = (1..length).map {
                decode(from, chType, type.type, types, adapters)
            }
            return ValueDefinition.Sequence(children.toTypedArray())
        }

        private fun decodeArray(
            type: RuntimeTypeDefArray, from: ByteArrayReader,
            types: RuntimeLookup, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> {
            val chType = types.findItemById(type.type)!!
            val chDef = chType.def
            if (chDef is RuntimeTypeDef.Primitive && chDef.primitive == U8) {
                val bytes = from.read(type.length.toInt())
                return ValueDefinition.Primitive(PrimitiveDefinition.Bytes(bytes))
            }
            val children = (1..type.length.toInt()).map {
                decode(from, chType, type.type, types, adapters)
            }
            return ValueDefinition.Sequence(children.toTypedArray())
        }

        private fun decodeTuple(
            type: RuntimeTypeDefTuple, from: ByteArrayReader,
            types: RuntimeLookup, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> = ValueDefinition.Sequence(
            type.types.map {
                decode(from, it, types, adapters)
            }.toTypedArray()
        )

        private fun decodeCompact(
            from: ByteArrayReader, adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> = ValueDefinition.Primitive(
            PrimitiveDefinition.Int(
                adapters.findAdapter(BigInteger::class).read(from, BigInteger::class)
            )
        )

        private fun decodePrimitive(
            type: RuntimeTypeDefPrimitive, from: ByteArrayReader,
            adapters: ScaleCodecAdapterProvider
        ): ValueDefinition<TypeId> = ValueDefinition.Primitive(
            when (type) {
                Bool -> PrimitiveDefinition.Bool(
                    adapters.findAdapter(Boolean::class).read(from, Boolean::class)
                )

                RuntimeTypeDefPrimitive.Char -> PrimitiveDefinition.Char(
                    adapters.findAdapter(UInt32::class).read(from, UInt32::class)
                )

                RuntimeTypeDefPrimitive.String -> PrimitiveDefinition.String(
                    adapters.findAdapter(String::class).read(from, String::class)
                )

                U8 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(UInt8::class).read(from, UInt8::class).toLong()
                    )
                )

                U16 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(UInt16::class).read(from, UInt16::class).toLong()
                    )
                )

                U32 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(UInt32::class).read(from, UInt32::class).toLong()
                    )
                )

                U64 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(UInt64::class).read(from, UInt64::class).toLong()
                    )
                )

                U128 -> PrimitiveDefinition.Int(
                    adapters.findAdapter(UInt128::class).read(from, UInt128::class).value
                )

                U256 -> PrimitiveDefinition.Int(
                    adapters.findAdapter(UInt256::class).read(from, UInt256::class).value
                )

                I8 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(Int8::class).read(from, Int8::class).toLong()
                    )
                )

                I16 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(Int16::class).read(from, Int16::class).toLong()
                    )
                )

                I32 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(Int32::class).read(from, Int32::class).toLong()
                    )
                )

                I64 -> PrimitiveDefinition.Int(
                    BigInteger.valueOf(
                        adapters.findAdapter(Int64::class).read(from, Int64::class).toLong()
                    )
                )

                I128 -> PrimitiveDefinition.Int(
                    adapters.findAdapter(Int128::class).read(from, Int128::class).value
                )

                I256 -> PrimitiveDefinition.Int(
                    adapters.findAdapter(Int256::class).read(from, Int256::class).value
                )
            }
        )
    }
}