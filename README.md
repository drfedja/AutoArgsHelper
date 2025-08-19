# NavGraphHelper

**NavGraphHelper** is a lightweight Android library and demo app that simplifies working with **Jetpack Navigation**, **Kotlin Serialization**, and **SavedStateHandle**. 
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
