package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.request.slack.SlackBotEventRequest
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import com.github.callibrity.chatbotreceiver.utils.JacksonMapper
import com.github.callibrity.chatbotreceiver.utils.SlackRequestVerifier
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class SlackRequestController(
    private val slackRequestVerifier: SlackRequestVerifier,
    private val jacksonMapper: JacksonMapper,
    private val chatBotService: ChatBotService
) {
    private val logger = LoggerFactory.getLogger(SlackRequestController::class.java)

    @PostMapping("/api/chatMessage")
    suspend fun chat(
        @RequestHeader("x-slack-signature") slackSignature: String?,
				@RequestHeader("x-slack-request-timestamp") timeStamp: String?,
				serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<String> {
				var bodyAsByteArray: ByteArray = ByteArray(0)

				serverHttpRequest.body
					.asFlow()
					.map { it.asInputStream() }
					.collect { inputStream ->
						bodyAsByteArray += inputStream.use { it.readBytes() }
					}

				val botEventRequest = jacksonMapper.deserializeBytesToObject(bodyAsByteArray, SlackBotEventRequest::class)

        return when (botEventRequest.type) {
            "url_verification" -> ResponseEntity(botEventRequest.challenge!!, HttpStatus.OK)
            "event_callback" -> {
                logger.info("Slack request pre verification...")
                if (isVerifiedRequest(slackSignature.orEmpty(), timeStamp.orEmpty(), bodyAsByteArray)) {
                    logger.info("Slack request verified...")
                    logger.info("Slack request text: ${botEventRequest.event?.text}")
                    chatBotService.chat(botEventRequest.event!!)

									ResponseEntity(HttpStatus.OK.toString(), HttpStatus.OK)
                } else {
                    logger.info("Slack request not verified...")

										ResponseEntity(HttpStatus.BAD_REQUEST.toString(), HttpStatus.BAD_REQUEST)
                }
            }
            else -> ResponseEntity(HttpStatus.NOT_IMPLEMENTED.toString(), HttpStatus.NOT_IMPLEMENTED)
        }
    }

    private fun isVerifiedRequest(
				signature: String,
				timestamp: String,
        rawRequestBody: ByteArray
    ): Boolean =
        slackRequestVerifier.isVerifiedSlackRequest(
						signature,
						timestamp,
            rawRequestBody
        )
}
