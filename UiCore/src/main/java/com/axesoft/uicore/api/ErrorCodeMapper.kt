package com.axesoft.uicore.api

import com.axesoft.uicore.exception.ApiException

private const val ERROR_MESSAGE_400 = "Unknown error."
private const val ERROR_MESSAGE_404 = "Unavailable."
private const val ERROR_MESSAGE_500 = "Unexpected error."
private const val ERROR_MESSAGE_503 = "Service Unavailable."
private const val ERROR_MESSAGE_409 = "the request is valid, but cannot be completed."
private const val DEFAULT_MESSAGE = "Please try again."

internal fun ApiException.toMessage() = when (this.message?.split(" ")?.firstOrNull()) {
    "400" -> ERROR_MESSAGE_400
    "404" -> ERROR_MESSAGE_404
    "500" -> ERROR_MESSAGE_500
    "503" -> ERROR_MESSAGE_503
    "409" -> ERROR_MESSAGE_409
    else -> DEFAULT_MESSAGE
}
