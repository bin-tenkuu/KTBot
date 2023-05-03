package my.ktbot.utils.global

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 *  @Date:2023/3/11
 *  @author bin
 *  @version 1.0.0
 */
val jsonGlobal = Json {
    classDiscriminator = "type"
    encodeDefaults = false
    ignoreUnknownKeys = true
    isLenient = false
    allowStructuredMapKeys = false
    prettyPrint = false
    coerceInputValues = true
    useArrayPolymorphism = false
    allowSpecialFloatingPointValues = true
    useAlternativeNames = true
}
