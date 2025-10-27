package com.axesoft.autoargsdestination

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Assert
import org.junit.Test

class AutoArgsDestinationTest {

    @Serializable
    data class TestArgs(val intValue: Int, val stringValue: String, val boolValue: Boolean, val floatValue: Float, val longValue: Long)

    object TestDestination : AutoArgsDestination<TestArgs>(TestArgs.serializer()) {
        override val baseRoute: String = "test"
        override val navGraphRoute: String = "test_graph"
        override val argumentSerializers: Map<String, KSerializer<out Any>>
            get() = TODO("Not yet implemented")
    }

    @Serializable
    data class TestArgsFallback(val id: Int, val custom: String)

    @Test
    fun `route should include argument placeholders`() {
        val route = TestDestination.route
        assert(route.contains("{intValue}"))
        assert(route.contains("{stringValue}"))
        assert(route.contains("{boolValue}"))
        assert(route.contains("{floatValue}"))
        assert(route.contains("{longValue}"))
    }

    @Test
    fun `buildRoute should inject args correctly`() {
        val args = TestArgs(7, "Hello", true, 3.14f, 123L)
        val route = TestDestination.buildRoute(args)
        assert(route.contains("intValue=7"))
        assert(route.contains("stringValue=Hello"))
        assert(route.contains("boolValue=true"))
        assert(route.contains("floatValue=3.14"))
        assert(route.contains("longValue=123"))
    }

    @Test
    fun `getArguments from SavedStateHandle should work`() {
        val args = TestArgs(42, "Fedja", true, 2.71f, 555L)
        val map = mapOf(
            "intValue" to args.intValue,
            "stringValue" to args.stringValue,
            "boolValue" to args.boolValue,
            "floatValue" to args.floatValue,
            "longValue" to args.longValue
        )
        val handle = SavedStateHandle(map)
        val result = TestDestination.getArguments(handle)
        Assert.assertEquals(args, result)
    }

    @Test
    fun `getArguments from Bundle should work`() {
        val args = TestArgs(99, "BundleTest", false, 1.23f, 999L)
        val bundle = mockk<Bundle>(relaxed = true)
        every { bundle.getInt("intValue") } returns args.intValue
        every { bundle.getString("stringValue") } returns args.stringValue
        every { bundle.getBoolean("boolValue") } returns args.boolValue
        every { bundle.getFloat("floatValue") } returns args.floatValue
        every { bundle.getLong("longValue") } returns args.longValue

        val result = TestDestination.getArguments(bundle)
        Assert.assertEquals(args, result)
    }

    @Test
    fun `fallback nav type should be handled correctly`() {
        val args = TestArgsFallback(1, "fallback")
        val customArg: NamedNavArgument = navArgument("custom") {
            type = object : NavType<Any>(true) {
                override fun get(bundle: Bundle, key: String): Any? = "fallback"
                override fun parseValue(value: String): Any = value
                override fun put(bundle: Bundle, key: String, value: Any) {}
            }
        }

        val navArgs = listOf(navArgument("id") { type = NavType.IntType }, customArg)
        val bundle = mockk<Bundle>(relaxed = true)
        every { bundle.getInt("id") } returns args.id
        every { bundle.getString("custom") } returns args.custom

        val serializer = ArgsSerializer(TestArgsFallback.serializer())
        val result = serializer.getArguments(bundle, navArgs)
        Assert.assertEquals(args, result)
    }

    @Test
    fun `getArguments from SavedStateHandle should handle else branch`() {
        val args = TestArgsFallback(1, "fallback")
        val serializer = ArgsSerializer(TestArgsFallback.serializer())

        val customArg: NamedNavArgument = navArgument("custom") {
            type = object : NavType<Any>(true) {
                override fun get(bundle: Bundle, key: String): Any? = bundle.getString(key)
                override fun parseValue(value: String): Any = value
                override fun put(bundle: Bundle, key: String, value: Any) {}
            }
        }

        val navArgs = listOf(
            navArgument("id") { type = NavType.IntType },
            customArg
        )

        val handle = SavedStateHandle(mapOf("id" to args.id, "custom" to args.custom))
        val result = serializer.getArguments(handle, navArgs)

        Assert.assertEquals(args, result)
    }

    @ExperimentalSerializationApi
    class DummySerializer : KSerializer<Unit> {
        @OptIn(SealedSerializationApi::class)
        override val descriptor: SerialDescriptor = object : SerialDescriptor {
            override val serialName: String = "Dummy"
            override val kind: SerialKind = StructureKind.CLASS
            override val elementsCount: Int = 1

            override fun getElementName(index: Int): String = "customField"
            override fun getElementIndex(name: String): Int = 0
            override fun getElementDescriptor(index: Int): SerialDescriptor = this
            override fun isElementOptional(index: Int): Boolean = false
            override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()
        }

        override fun serialize(encoder: Encoder, value: Unit) {}
        override fun deserialize(decoder: Decoder): Unit = Unit
    }

    @OptIn(ExperimentalSerializationApi::class)
    object DummyDestination : AutoArgsDestination<Unit>(DummySerializer()) {
        override val baseRoute: String = "dummy"
        override val navGraphRoute: String = "dummy_graph"
        override val argumentSerializers: Map<String, KSerializer<out Any>>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `buildNavArguments else branch is covered via AutoArgsDestination`() {
        val navArgs = DummyDestination.arguments
        val customArg = navArgs.find { it.name == "customField" }!!

        val argType = customArg.argument.type
        Assert.assertTrue(argType::class == NavType.StringType::class)
    }
}
