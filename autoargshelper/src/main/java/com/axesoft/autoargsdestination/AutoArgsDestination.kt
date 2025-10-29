package com.axesoft.autoargsdestination

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

abstract class AutoArgsDestination<T : Any>(
    serializer: KSerializer<T>
) {
    private val argsSerializer: ArgsSerializer<T> = ArgsSerializer(serializer)

    abstract val baseRoute: String
    abstract val navGraphRoute: String
    open var complexArgumentsSerializers: Map<String, KSerializer<out Any>> = emptyMap()

    val arguments: List<NamedNavArgument> by lazy { argsSerializer.buildNavArguments() }

    val route: String
        get() = "$baseRoute?" + arguments.joinToString("&") { "${it.name}={${it.name}}" }

    /**
     * Builds a navigation route string using the provided [args].
     * Serializes all arguments into a valid route format.
     * Commonly used when triggering navigation events.
     */
    fun buildRoute(args: T): String =
        argsSerializer.buildRoute(args, baseRoute)

    /**
     * Retrieves primitive and simple typed arguments directly from [SavedStateHandle].
     *
     * Used inside a ViewModel where Hilt automatically provides [SavedStateHandle].
     * Handles only primitive navigation arguments (Int, String, Boolean, etc.),
     * and relies on [argsSerializer] for consistent deserialization.
     */
    fun getArguments(savedStateHandle: SavedStateHandle): T =
        argsSerializer.getArguments(savedStateHandle, arguments)

    /**
     * Retrieves primitive and simple typed arguments from a [Bundle].
     *
     * Useful when navigating through traditional Fragment APIs where
     * arguments are still passed as a [Bundle].
     * This function mirrors the behavior of [getArguments] for SavedStateHandle.
     */
    fun getArguments(bundle: Bundle?): T =
        argsSerializer.getArguments(bundle, arguments)


    /**
     * Extension function for [SavedStateHandle] that supports both primitive and complex objects.
     *
     * Unlike the other two variants, this function can reconstruct full data classes
     * containing nested @Serializable objects (e.g. DTOs, UI models).
     * It works for both complex and primitive types.
     * It uses [complexArgumentsSerializers] to resolve each argument’s serializer,
     * deserializes them from their encoded string form,
     * and rebuilds the full argument data class via [mapToArgs].
     */
    inline fun <reified T : Any> SavedStateHandle.getComplexArgs(): T {
        val argsMap: Map<String, Any?> =
            complexArgumentsSerializers.mapValues { (name, serializer) ->
                when (serializer) {
                    String.serializer() -> this[name] as? String
                    else -> {
                        val str = this[name] as? String ?: return@mapValues null
                        if (str.isBlank()) null else str.toOriginalObject(serializer)
                    }
                }
            }
        return mapToArgs(argsMap)
    }

    /**
     * Generates a map of property names to their corresponding Kotlinx Serialization
     * serializers for a given data class [T].
     *
     * This function uses reflection to inspect all member properties of [T] and attempts
     * to automatically provide serializers for:
     *   - Data classes (nested objects)
     *   - Primitive types: String, Int, Boolean, Long, Float, Double
     *
     * Unsupported types are skipped and printed to the console for debugging.
     *
     * @return A map where keys are property names and values are their [KSerializer] instances.
     * @throws Any exceptions encountered while retrieving serializers are caught and logged.
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> generateArgumentSerializers(): Map<String, KSerializer<out Any>> {
        val kClass: KClass<T> = T::class
        val map = mutableMapOf<String, KSerializer<out Any>>()

        kClass.memberProperties.forEach { prop ->
            val propClass = prop.returnType.classifier as? KClass<*>
            if (propClass != null) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val serializer = when {
                        propClass.isData -> propClass.serializer() as KSerializer<Any>
                        propClass == String::class -> String.serializer()
                        propClass == Int::class -> Int.serializer()
                        propClass == Boolean::class -> Boolean.serializer()
                        propClass == Long::class -> Long.serializer()
                        propClass == Float::class -> Float.serializer()
                        propClass == Double::class -> Double.serializer()
                        else -> null
                    }
                    if (serializer != null) {
                        map[prop.name] = serializer
                    } else {
                        println("Skipping unsupported type: ${prop.name} -> ${prop.returnType}")
                    }
                } catch (e: Exception) {
                    println("Could not get serializer for ${prop.name}: ${e.message}")
                }
            }
        }
        return map
    }
}
