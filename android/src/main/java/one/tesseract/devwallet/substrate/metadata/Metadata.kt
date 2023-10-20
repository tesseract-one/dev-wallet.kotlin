package one.tesseract.devwallet.substrate.metadata

import java.math.BigInteger

import dev.sublab.common.numerics.*
import dev.sublab.scale.annotations.*

data class TypeId(val id: BigInteger) : ValueContext {
    override fun toString(): String = "#$id"
}

/**
 * Runtime lookup. Holds an array of lookup items
 */
data class RuntimeLookup(
    private val items: List<RuntimeLookupItem>
) {

    private var itemsById: Map<TypeId, RuntimeType> = items.associate { Pair(it.id, it.type) }

    /**
     * Finds a lookup item by an index of type Int
     * @param id id (of type TypeId) to find a lookup item
     * @return A lookup item for a specific index
     */
    fun findItemById(id: TypeId): RuntimeType? = itemsById[id]
}

/**
 * TypeId -> RuntimeType Pair
 */
data class RuntimeLookupItem(
    val id: TypeId,
    val type: RuntimeType
)

/**
 * Runtime type parameter
 */
data class RuntimeTypeParam(
    val name: String,
    val type: TypeId?
)

/**
 * Runtime type
 */
data class RuntimeType(
    val path: List<String>,
    val params: List<RuntimeTypeParam>,
    val def: RuntimeTypeDef,
    val docs: List<String>
)

/**
 * Runtime type definition
 */
@Suppress("unused")
@EnumClass
sealed class RuntimeTypeDef {
    @EnumCase(0) data class Composite(val composite: RuntimeTypeDefComposite): RuntimeTypeDef()
    @EnumCase(1) data class Variant(val variant: RuntimeTypeDefVariant): RuntimeTypeDef()
    @EnumCase(2) data class Sequence(val sequence: RuntimeTypeDefSequence): RuntimeTypeDef()
    @EnumCase(3) data class Array(val array: RuntimeTypeDefArray): RuntimeTypeDef()
    @EnumCase(4) data class Tuple(val tuple: RuntimeTypeDefTuple): RuntimeTypeDef()
    @EnumCase(5) data class Primitive(val primitive: RuntimeTypeDefPrimitive): RuntimeTypeDef()
    @EnumCase(6) data class Compact(val compact: RuntimeTypeDefCompact): RuntimeTypeDef()
    @EnumCase(7) data class BitSequence(val bitSequence: RuntimeTypeDefBitSequence): RuntimeTypeDef()
}

/**
 * Array runtime type
 */
data class RuntimeTypeDefArray(
    val length: UInt32,
    val type: TypeId
)

/**
 * Bit sequence runtime type
 */
data class RuntimeTypeDefBitSequence(
    val store: TypeId,
    val order: TypeId
)

/**
 * Compact runtime typ
 */
data class RuntimeTypeDefCompact(
    val type: TypeId
)

/**
 * Composite runtime type
 */
data class RuntimeTypeDefComposite(
    val fields: List<RuntimeTypeDefField>
)

/**
 * Runtime type field
 */
data class RuntimeTypeDefField(
    val name: String?,
    val type: TypeId,
    val typeName: String?,
    val docs: List<String>
)

/**
 * Primitive runtime type
 */
enum class RuntimeTypeDefPrimitive {
    Bool, Char, String,
    U8, U16, U32, U64, U128, U256,
    I8, I16, I32, I64, I128, I256
}

/**
 * Sequence runtime type
 */
data class RuntimeTypeDefSequence(
    val type: TypeId
)


/**
 * Tuple runtime type
 */
data class RuntimeTypeDefTuple(
    val types: List<TypeId>
)

/**
 * Variant runtime type
 */
data class RuntimeTypeDefVariant(
    val variants: List<Variant>
) {

    data class Variant(
        val name: String,
        val fields: List<RuntimeTypeDefField>,
        internal val indexUInt8: UInt8,
        val docs: List<String>
    ) {
        val index get() = indexUInt8.toUInt()
    }
}

/**
 * Runtime extrinsic. Contains its type, version and an array of signed extensions
 */
data class RuntimeExtrinsic(
    val type: TypeId,
    val version: UInt8,
    val signedExtensions: List<SignedExtension>
) {

    /**
     * Signed extrinsic. Contains its identifier, type and an additional signed
     */
    data class SignedExtension(
        val identifier: String,
        val type: TypeId,
        val additionalSigned: TypeId
    )
}