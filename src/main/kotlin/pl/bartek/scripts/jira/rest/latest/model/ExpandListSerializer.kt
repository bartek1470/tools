package pl.bartek.scripts.jira.rest.latest.model

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val log = KotlinLogging.logger {}

object ExpandListSerializer : KSerializer<List<Expand>> {

    override val descriptor = PrimitiveSerialDescriptor("Expand", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): List<Expand> {
        val fieldVal = decoder.decodeString()
        return fieldVal.split(",")
            .mapNotNull {
                val expand = Expand.byId(it)
                if (expand == null) log.warn { "Unrecognized ${descriptor.serialName} value: '$it'" }
                expand
            }
    }

    override fun serialize(encoder: Encoder, value: List<Expand>) {
        val serializedValue = value.joinToString(",") { it.id }
        encoder.encodeString(serializedValue)
    }
}
