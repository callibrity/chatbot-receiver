package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val chatBotService: ChatBotService
) {

    @GetMapping("/health")
    fun health(): ResponseEntity<ApiResponse<String>> {
        val result = chatBotService.heartBeat()

        return ResponseEntity(ApiResponse(
            data = result,
            meta = Meta("Ok", "v1")
        ), HttpStatus.OK)
    }
}
