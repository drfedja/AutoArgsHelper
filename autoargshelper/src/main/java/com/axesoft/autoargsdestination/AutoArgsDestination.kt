package com.axesoft.autoargsdestination

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import kotlinx.serialization.KSerializer

abstract class AutoArgsDestination<T : Any>(
    serializer: KSerializer<T>
) {
    private val argsSerializer: ArgsSerializer<T> = ArgsSerializer(serializer)

    abstract val baseRoute: String
    abstract val navGraphRoute: String
    abstract val argumentSerializers: Map<String, KSerializer<out Any>>

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
     * It uses [argumentSerializers] to resolve each argument’s serializer,
     * deserializes them from their encoded string form,
     * and rebuilds the full argument data class via [mapToArgs].
     */
    inline fun <reified T : Any> SavedStateHandle.getComplexArgs(): T {
        val argsMap: Map<String, Any?> = argumentSerializers.mapValues { (name, serializer) ->
            val str = this[name] as? String ?: ""
            if (str.isBlank()) null
            else str.toOriginalObject(serializer)
        }

        return mapToArgs<T>(argsMap)
    }
}
