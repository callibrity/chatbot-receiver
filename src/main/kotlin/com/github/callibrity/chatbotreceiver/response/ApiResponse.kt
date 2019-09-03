package com.github.callibrity.chatbotreceiver.response

data class Meta(
    val status: String?,
    val version: String?
)

data class Error(
    val code: String?,
    val cause: String?,
    val timestamp: Number?
)

data class ApiResponse<T>(
    val data: T?,
    val meta: Meta? = null,
    val errors: List<Error>? = null
)
