package com.axesoft.uicore.navigation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

sealed class ScreenResult<T> {
    abstract val data: T?
    abstract fun getUUID(): String
    abstract fun putDataToResult(savedState: SavedStateHandle?)

    internal fun consumeResult(savedState: SavedStateHandle?): T? {
        return savedState?.let {
            savedState.get<T>(this.getUUID())?.let {
                savedState.remove<T>(getUUID())
                it
            }
        }
    }

    protected fun putData(savedState: SavedStateHandle?) {
        savedState?.let {
            data?.let {
                savedState[this@ScreenResult.getUUID()] = it
            }
        }
    }
}

abstract class BooleanScreenResult : ScreenResult<Boolean>() {
    override fun putDataToResult(savedState: SavedStateHandle?) {
        putData(savedState)
    }
}

abstract class NumberScreenResult<T : Number> : ScreenResult<T>() {
    override fun putDataToResult(savedState: SavedStateHandle?) {
        putData(savedState)
    }
}

abstract class StringScreenResult : ScreenResult<String>() {
    override fun putDataToResult(savedState: SavedStateHandle?) {
        putData(savedState)
    }
}

abstract class ParcelableScreenResult<T : Parcelable> : ScreenResult<T>() {
    override fun putDataToResult(savedState: SavedStateHandle?) {
        putData(savedState)
    }
}
