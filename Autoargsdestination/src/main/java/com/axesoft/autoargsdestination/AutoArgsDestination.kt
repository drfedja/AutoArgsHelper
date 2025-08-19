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

    val arguments: List<NamedNavArgument> by lazy { argsSerializer.buildNavArguments() }

    val route: String
        get() = "$baseRoute?" + arguments.joinToString("&") { "${it.name}={${it.name}}" }

    fun getArguments(savedStateHandle: SavedStateHandle): T =
        argsSerializer.getArguments(savedStateHandle, arguments)

    fun getArguments(bundle: Bundle?): T =
        argsSerializer.getArguments(bundle, arguments)

    fun buildRoute(args: T): String =
        argsSerializer.buildRoute(args, baseRoute, arguments)


    fun <R> toNavArg(value: R, serializer: KSerializer<R>): String =
        argsSerializer.objectToNavArg(value, serializer)

    fun <R> fromNavArg(string: String, serializer: KSerializer<R>): R? =
        argsSerializer.toOriginalObject(string, serializer)
}