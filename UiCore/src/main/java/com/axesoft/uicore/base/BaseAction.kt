package com.axesoft.uicore.base

abstract class BaseAction {
    abstract val eventLog: EventLogging
}

sealed interface EventLogging {
    @JvmInline value class Logging(val description: String) : EventLogging
    object NonLogging : EventLogging
}