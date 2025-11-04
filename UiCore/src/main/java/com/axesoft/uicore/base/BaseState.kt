package com.axesoft.uicore.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.util.Locale

@Stable
@Immutable
abstract class BaseState<Action> {
    open val availableLanguages: List<Locale> = listOf()
    open val currentLocale: Locale = Locale.getDefault()
    abstract val onAction: (Action) -> Unit
}
