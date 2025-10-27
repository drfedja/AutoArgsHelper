package com.axesoft.autoargsdestination

import com.axesoft.autoargsdestination.NavArgEncodingConstants.AMPERSAND
import com.axesoft.autoargsdestination.NavArgEncodingConstants.AMPERSAND_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.CARRIAGE_RETURN
import com.axesoft.autoargsdestination.NavArgEncodingConstants.CARRIAGE_RETURN_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.DOLLAR_SIGN
import com.axesoft.autoargsdestination.NavArgEncodingConstants.DOLLAR_SIGN_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.EQUALS_SIGN
import com.axesoft.autoargsdestination.NavArgEncodingConstants.EQUALS_SIGN_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.FORWARD_SLASH
import com.axesoft.autoargsdestination.NavArgEncodingConstants.FORWARD_SLASH_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.LEFT_CURLY_BRACKET
import com.axesoft.autoargsdestination.NavArgEncodingConstants.LEFT_CURLY_BRACKET_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.NEW_LINE
import com.axesoft.autoargsdestination.NavArgEncodingConstants.NEW_LINE_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.PERCENTAGE
import com.axesoft.autoargsdestination.NavArgEncodingConstants.PERCENTAGE_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.PLUS_SIGN
import com.axesoft.autoargsdestination.NavArgEncodingConstants.PLUS_SIGN_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.QUESTION_MARK
import com.axesoft.autoargsdestination.NavArgEncodingConstants.QUESTION_MARK_UNI
import com.axesoft.autoargsdestination.NavArgEncodingConstants.RIGHT_CURLY_BRACKET
import com.axesoft.autoargsdestination.NavArgEncodingConstants.RIGHT_CURLY_BRACKET_UNI
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer

/**
 * Converts object (including list of objects) to String for passing data between destinations
 * Custom Object should be Serializable
 */
internal inline fun <reified T> T.objectToNavArg(): String {
    return try {
        encodeStringToArg(
            value = Json.encodeToString(this)
        )
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            throw e
        } else {
            ""
        }
    }
}

/**
 * Converts String back to original Object
 */
fun <T> String.toOriginalObject(serializer: KSerializer<T>): T? {
    val jsonIgnoreUnknown = Json {
        ignoreUnknownKeys = true
    }
    return try {
        jsonIgnoreUnknown.decodeFromString(
            deserializer = serializer,
            string = decodeStringFromArg(this)
        )
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) throw e else null
    }
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> mapToArgs(
    argsMap: Map<String, Any?>
): T {
    val jsonMap = argsMap.mapValues { (_, v) ->
        when (v) {
            is Boolean -> JsonPrimitive(v)
            is Int -> JsonPrimitive(v)
            is Float -> JsonPrimitive(v)
            is Long -> JsonPrimitive(v)
            is String -> JsonPrimitive(v)
            null -> JsonNull
            else -> Json.encodeToJsonElement(
                v::class.serializer() as KSerializer<Any>, v
            )
        }
    }
    val jsonObject = JsonObject(jsonMap)
    return Json.decodeFromJsonElement(T::class.serializer(), jsonObject)
}

/**
 * Converts String to save arg
 */
internal fun encodeStringToArg(value: String): String =
    value
        .replace(PERCENTAGE, PERCENTAGE_UNI)
        .replace(AMPERSAND, AMPERSAND_UNI)
        .replace(EQUALS_SIGN, EQUALS_SIGN_UNI)
        .replace(QUESTION_MARK, QUESTION_MARK_UNI)
        .replace(FORWARD_SLASH, FORWARD_SLASH_UNI)
        .replace(DOLLAR_SIGN, DOLLAR_SIGN_UNI)
        .replace(LEFT_CURLY_BRACKET, LEFT_CURLY_BRACKET_UNI)
        .replace(RIGHT_CURLY_BRACKET, RIGHT_CURLY_BRACKET_UNI)
        .replace(PLUS_SIGN, PLUS_SIGN_UNI)
        .replace(NEW_LINE, NEW_LINE_UNI)
        .replace(CARRIAGE_RETURN, CARRIAGE_RETURN_UNI)

/**
 * Recovers original String
 * Uses together with [encodeStringToArg]
 */
internal fun decodeStringFromArg(value: String): String =
    value
        .replace(PERCENTAGE_UNI, PERCENTAGE)
        .replace(AMPERSAND_UNI, AMPERSAND)
        .replace(EQUALS_SIGN_UNI, EQUALS_SIGN)
        .replace(QUESTION_MARK_UNI, QUESTION_MARK)
        .replace(FORWARD_SLASH_UNI, FORWARD_SLASH)
        .replace(DOLLAR_SIGN_UNI, DOLLAR_SIGN)
        .replace(LEFT_CURLY_BRACKET_UNI, LEFT_CURLY_BRACKET)
        .replace(RIGHT_CURLY_BRACKET_UNI, RIGHT_CURLY_BRACKET)
        .replace(PLUS_SIGN_UNI, PLUS_SIGN)
        .replace(NEW_LINE_UNI, NEW_LINE)
        .replace(CARRIAGE_RETURN_UNI, CARRIAGE_RETURN)
