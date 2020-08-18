package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Meta
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import com.ninjasquad.springmockk.MockkBean
import com.proto.chatbot.ChatbotResponse
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(TestController::class)
class TestControllerTest(@Autowired val client: WebTestClient) {

	@MockkBean
	private lateinit var chatBotService: ChatBotService

	@Test
	fun `testChat test`() {
		val chatbotResponse = ChatbotResponse.newBuilder()
			.setAnswer("Hello")
			.build()

		coEvery { chatBotService.chat(any<String>()) } returns chatbotResponse

		client.post()
			.uri("/api/test/message")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(TestController.TestRequest("Hi"))
			.exchange()
			.expectStatus()
			.isOk
			.expectBody<ApiResponse<String>>()
			.isEqualTo(ApiResponse("Hello", meta = Meta("Ok", "v1")))
	}
}
