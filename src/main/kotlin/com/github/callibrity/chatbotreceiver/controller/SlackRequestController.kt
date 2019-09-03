package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.request.slack.SlackBotEventRequest
import com.github.callibrity.chatbotreceiver.service.grpc.ChatBotService
import com.github.callibrity.chatbotreceiver.utils.JacksonMapper
import com.github.callibrity.chatbotreceiver.utils.SlackRequestVerifier
import com.google.common.io.ByteStreams
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class SlackRequestController(
    private val slackRequestVerifier: SlackRequestVerifier,
    private val jacksonMapper: JacksonMapper,
    private val chatBotService: ChatBotService
) {

    @PostMapping("/api/chatMessage")
    fun chat(
        @RequestHeader headers: Map<String, String>,
        rawRequest: HttpServletRequest
    ): ResponseEntity<String> {
        val bodyAsByteArray = ByteStreams.toByteArray(rawRequest.inputStream)
        val botEventRequest = jacksonMapper.deserializeBytesToObject(bodyAsByteArray, SlackBotEventRequest::class)

        return when (botEventRequest.type) {
            "url_verification" -> ResponseEntity(botEventRequest.challenge!!, HttpStatus.OK)
            "event_callback" -> {
                if (slackRequestVerifier.isVerifiedSlackRequest(
                        headers["x-slack-signature"]!!,
                        headers["x-slack-request-timestamp"]!!,
                        bodyAsByteArray
                    )) {
                    val event = botEventRequest.event!!
                    chatBotService.chat(event.text!!, event.user!!, event.channel!!)
                    ResponseEntity("Ok", HttpStatus.OK)
                } else {
                    ResponseEntity("Not Found", HttpStatus.NOT_FOUND)
                }
            }
            else -> ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }
}
