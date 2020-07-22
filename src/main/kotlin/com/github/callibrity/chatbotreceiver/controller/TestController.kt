package com.github.callibrity.chatbotreceiver.controller

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val chatBotService: ChatBotService
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TestRequest(
        val message: String
    )

    @PostMapping("/api/test/message")
    suspend fun testChat(@RequestBody testRequest: TestRequest): ApiResponse<String> {
        val result = chatBotService.chat(testRequest.message)

        return ApiResponse(
            data = result.answer,
            meta = Meta("Ok", "v1")
        )
    }
}
