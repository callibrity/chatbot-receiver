package com.github.callibrity.chatbotreceiver.request.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(
    val type: String,
    val subtype: String?,
    val text: String?,
    val user: String?,
    val channel: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackBotEventRequest (
    val type: String,
    val challenge: String?,
    val event: Event?
)
