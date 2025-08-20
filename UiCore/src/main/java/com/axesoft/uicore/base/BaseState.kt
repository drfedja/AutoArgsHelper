package com.axesoft.uicore.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
abstract class BaseState<Action> {
    abstract val onAction: (Action) -> Unit
}
