package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val chatBotService: ChatBotService
) {

    @GetMapping("/health")
    suspend fun health(): ApiResponse<String> {
        val result = chatBotService.heartBeat()

        return ApiResponse(
            data = result,
            meta = Meta("Ok", "v1")
        )
    }
}
