package com.axesoft.autoargsdestination

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.collections.associate

internal open class ArgsSerializer<T : Any>(
    private val serializer: KSerializer<T>
) {
    fun getArguments(savedStateHandle: SavedStateHandle, arguments: List<NamedNavArgument>): T {
        val rawMap = arguments.associate { arg ->
            val value: JsonPrimitive = when (arg.argument.type) {
                NavType.BoolType -> JsonPrimitive(savedStateHandle[arg.name] ?: false)
                NavType.IntType -> JsonPrimitive(savedStateHandle[arg.name] ?: 0)
                NavType.FloatType -> JsonPrimitive(savedStateHandle[arg.name] ?: 0f)
                NavType.LongType -> JsonPrimitive(savedStateHandle[arg.name] ?: 0L)
                NavType.StringType -> JsonPrimitive(savedStateHandle[arg.name] ?: "")
                else -> JsonPrimitive(savedStateHandle[arg.name] ?: "")
            }
            arg.name to value
        }
        return Json.decodeFromJsonElement(
            deserializer = serializer,
            element = JsonObject(rawMap)
        )
    }

    fun getArguments(bundle: Bundle?, arguments: List<NamedNavArgument>): T {
        val rawMap = arguments.associate { arg ->
            val value: JsonPrimitive = when (arg.argument.type) {
                NavType.BoolType -> JsonPrimitive(
                    bundle?.getBoolean(arg.name) ?: false
                )

                NavType.IntType -> JsonPrimitive(bundle?.getInt(arg.name) ?: 0)
                NavType.FloatType -> JsonPrimitive(bundle?.getFloat(arg.name) ?: 0f)
                NavType.LongType -> JsonPrimitive(bundle?.getLong(arg.name) ?: 0L)
                NavType.StringType -> JsonPrimitive(bundle?.getString(arg.name) ?: "")
                else -> JsonPrimitive(bundle?.getString(arg.name) ?: "")
            }
            arg.name to value
        }
        return Json.decodeFromJsonElement(
            deserializer = serializer,
            element = JsonObject(rawMap)
        )
    }

    fun buildRoute(args: T, baseRoute: String): String {
        val map = Json.encodeToJsonElement(serializer, args).jsonObject
        val query = map.entries.joinToString("&")
        { (key, value) ->
            when (value) {
                is JsonPrimitive -> "$key=${encodeStringToArg(value.content)}"
                else -> "$key=${value.objectToNavArg()}"
            }
        }
        return "$baseRoute?$query"
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun buildNavArguments(): List<NamedNavArgument> {
        val descriptor = serializer.descriptor
        return List(descriptor.elementsCount) { index ->
            val name = descriptor.getElementName(index)
            val elementDescriptor = descriptor.getElementDescriptor(index)
            val kind = elementDescriptor.kind

            navArgument(name) {
                type = when (kind) {
                    PrimitiveKind.STRING -> NavType.StringType
                    PrimitiveKind.INT -> NavType.IntType
                    PrimitiveKind.BOOLEAN -> NavType.BoolType
                    PrimitiveKind.FLOAT -> NavType.FloatType
                    PrimitiveKind.LONG -> NavType.LongType
                    else -> NavType.StringType
                }
            }
        }
    }
}
