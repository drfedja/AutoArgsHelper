# AutoArgsHelper

**AutoArgsHelper** is a lightweight Android library and demo app that simplifies working with **Jetpack Navigation**, **Kotlin Serialization**, and **SavedStateHandle**. 
It allows you to easily define navigation destinations using `@Serializable` data classes, automatically passing and retrieving typed arguments between screens.

---

## Features

- **AutoArgsDestination**: Define navigation destinations with typed arguments.
- Automatic parsing of arguments from `SavedStateHandle` without manual bundles.
- Full support for Jetpack Compose.
- Demo app with two screens showcasing real-world usage.
- Fully tested: unit tests cover serialization and argument handling 100%.

---

## Installation

Add the library module to your project:

```kotlin
implementation(project(":Autoargsdestination"))
```

Make sure you have these dependencies in your libs.versions.toml:

```toml
[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```

Usage:

Define your navigation destination with a @Serializable data class:

```kotlin
@Serializable
data class FirstScreenArgs(val userId: Int, val userName: String)

object FirstScreenDestination : AutoArgsDestination<FirstScreenArgs>(FirstScreenArgs.serializer()) {
    override val baseRoute: String = "firstScreen"
    override val navGraphRoute: String = "demoGraph"
}
```
Create a NavHost in Composable:

```kotlin
@Composable
fun DemoNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FirstScreenDestination.buildRoute(
            FirstScreenArgs(userId = 123, userName = "UserName")
        )
    ) {
        composable(
            route = FirstScreenDestination.route,
            arguments = FirstScreenDestination.arguments
        ) { backStackEntry ->
            val args = FirstScreenDestination.getArguments(backStackEntry.savedStateHandle)
            FirstScreen(args) { message ->
                val secondArgs = SecondScreenArgs(message)
                navController.navigate(SecondScreenDestination.buildRoute(secondArgs))
            }
        }

        composable(
            route = SecondScreenDestination.route,
            arguments = SecondScreenDestination.arguments
        ) { backStackEntry ->
            val args = SecondScreenDestination.getArguments(backStackEntry.savedStateHandle)
            SecondScreen(args)
        }
    }
}
```

## Flow chart

```
SavedStateHandle
      │
      │  (contains raw arguments: Strings, serialized JSON, primitives)
      ▼
───────────────────────────────
| map argumentSerializers     |   ← argumentSerializers: Map<String, KSerializer<out Any>>
|  (KSerializer for complex    |
|   and primitive types)      |
───────────────────────────────
      │
      │  getComplexArgs<T>() reads each argument:
      │    - if primitive: use directly
      │    - if complex: decode JSON using serializer
      ▼
───────────────────────────────
| argsMap: Map<String, Any?>  |   ← deserialized values
|  key1 -> ComplexObject(...) |
|  key2 -> "string value"     |
───────────────────────────────
      │
      │  mapToArgs<T>(argsMap)
      │    - converts primitives to JsonPrimitive
      │    - converts complex objects to JsonElements
      │    - builds JsonObject
      │    - decodes to target data class T
      ▼
───────────────────────────────
| T Data Class Object         |   ← fully typed object
|  complexArg: ComplexType?   |
|  primitiveArg: String       |
───────────────────────────────
      │
      │  Returned to ViewModel / Hilt
      ▼
ViewModel / Hilt injection
      │
      │  type-safe usage:
      │    args.complexArg?.field
      │    args.primitiveArg
      ▼
Usage in UI / Business Logic
```

## Demo App

The included demo app shows:
* A first screen with input fields.
* Navigation to a second screen with typed arguments.
* Full Compose integration.

## Technologies

* Kotlin + Kotlin Serialization
* Jetpack Compose
* Android Navigation
* SavedStateHandle
* JUnit + MockK for testing

## License

MIT License. Feel free to use and adapt for your own projects.
