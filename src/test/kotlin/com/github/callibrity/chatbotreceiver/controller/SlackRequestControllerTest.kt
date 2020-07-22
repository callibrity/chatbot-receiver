package com.github.callibrity.chatbotreceiver.controller

import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.github.callibrity.chatbotreceiver.request.slack.SlackBotEventRequest
import com.github.callibrity.chatbotreceiver.service.ChatBotService
import com.github.callibrity.chatbotreceiver.utils.JacksonMapper
import com.github.callibrity.chatbotreceiver.utils.SlackRequestVerifier
import com.ninjasquad.springmockk.MockkBean
import com.proto.chatbot.ChatbotResponse
import io.mockk.coEvery
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(SlackRequestController::class)
class SlackRequestControllerTest(@Autowired val client: WebTestClient) {

	@MockkBean
	private lateinit var slackRequestVerifier: SlackRequestVerifier

	@MockkBean
	private lateinit var jacksonMapper: JacksonMapper

	@MockkBean
	private lateinit var chatBotService: ChatBotService

	@Test
	fun `should return challenge from slack url verification request`() {
		val urlVerificationRequest = SlackBotEventRequest(
			"url_verification",
			"3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
			null
		)

		every {
			jacksonMapper.deserializeBytesToObject(any(), SlackBotEventRequest::class)
		} returns urlVerificationRequest

		client.post()
			.uri("/api/chatMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("""
				{
				    "token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
				    "challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
				    "type": "url_verification"
				}
			""".trimIndent())
			.exchange()
			.expectStatus()
			.isOk
			.expectBody<String>()
			.isEqualTo("3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P")
	}

	@Test
	fun `should return 200 ok for verified event_callback slack request`() {
		every {
			jacksonMapper.deserializeBytesToObject(any(), SlackBotEventRequest::class)
		} returns SlackBotEventRequest("event_callback", null, Event(
			"app_mention", null, "Hello", "test", "test"
		))

		every { slackRequestVerifier.isVerifiedSlackRequest(any(), any(), any()) } returns true

		coEvery {
			chatBotService.chat(any<Event>())
		} returns ChatbotResponse.newBuilder().setAnswer("Hello").build()

		client.post()
			.uri("/api/chatMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("""
				{
						"token": "ZZZZZZWSxiZZZ2yIvs3peJ",
						"team_id": "test team id",
						"api_app_id": "test app id",
						"event": {
								"type": "app_mention",
								"user": "test",
								"text": "Hello",
								"ts": "1515449438.000011",
								"channel": "test",
								"event_ts": "1515449438000011"
						},
						"type": "event_callback",
						"event_id": "test id",
						"event_time": 1515449438000011,
						"authed_users": [
								"test"
						]
				}
			""".trimIndent())
			.exchange()
			.expectStatus()
			.isOk
			.expectBody<String>()
			.isEqualTo("200 OK")
	}

	@Test
	fun `should return 400 bad request for unverified event_callback slack request`() {
		every {
			jacksonMapper.deserializeBytesToObject(any(), SlackBotEventRequest::class)
		} returns SlackBotEventRequest("event_callback", null, Event(
			"app_mention", null, "Hello", "test", "test"
		))

		every { slackRequestVerifier.isVerifiedSlackRequest(any(), any(), any()) } returns false

		client.post()
			.uri("/api/chatMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("""
				{
						"token": "ZZZZZZWSxiZZZ2yIvs3peJ",
						"team_id": "test team id",
						"api_app_id": "test app id",
						"event": {
								"type": "app_mention",
								"user": "test",
								"text": "Hello",
								"ts": "1515449438.000011",
								"channel": "test",
								"event_ts": "1515449438000011"
						},
						"type": "event_callback",
						"event_id": "test id",
						"event_time": 1515449438000011,
						"authed_users": [
								"test"
						]
				}
			""".trimIndent())
			.exchange()
			.expectStatus()
			.isBadRequest
			.expectBody<String>()
			.isEqualTo("400 BAD_REQUEST")
	}

	@Test
	fun `should return 501 not implemented for other request`() {
		every {
			jacksonMapper.deserializeBytesToObject(any(), SlackBotEventRequest::class)
		} returns SlackBotEventRequest("test", null, Event(
			"app_mention", null, "Hello", "test", "test"
		))

		client.post()
			.uri("/api/chatMessage")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("""
				{
						"event": {
								"type": "app_mention",
								"user": "test",
								"text": "Hello",
								"ts": "1515449438.000011",
								"channel": "test",
								"event_ts": "1515449438000011"
						},
						"type": "test",
						"event_id": "test id",
						"event_time": 1515449438000011,
				}
			""".trimIndent())
			.exchange()
			.expectStatus()
			.is5xxServerError
			.expectBody<String>()
			.isEqualTo("501 NOT_IMPLEMENTED")
	}
}
