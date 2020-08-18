package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(HealthController::class)
class HealthControllerTest (@Autowired val client: WebTestClient) {

	@MockkBean
	private lateinit var chatbotService: ChatBotService

	@Test
	fun `health test`() {
		coEvery { chatbotService.heartBeat() } returns "test"

		client.get()
			.uri("/health")
			.exchange()
			.expectStatus()
			.isOk
			.expectBody<ApiResponse<String>>()
			.isEqualTo(ApiResponse("test", meta = Meta("Ok", "v1")))
	}
}
