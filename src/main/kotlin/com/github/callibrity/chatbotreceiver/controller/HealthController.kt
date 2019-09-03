package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): ApiResponse<String> = ApiResponse(
        data = "Hello, People",
        meta = Meta("Ok", "v1")
    )
}
