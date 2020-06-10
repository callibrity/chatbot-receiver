package com.github.callibrity.chatbotreceiver.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meta(
    val status: String? = null,
    val version: String? = null
) {
	companion object {
		val DEFAULT_META = Meta("Ok", "v1")
	}
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Error(
    val code: String? = null,
    val cause: String? = null,
    val timestamp: Number? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T : Any>(
    val data: T?,
    val meta: Meta? = null,
    val errors: List<Error>? = null
)
