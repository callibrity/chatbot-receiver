package com.github.callibrity.chatbotreceiver.service

import com.github.callibrity.chatbotreceiver.grpc.client.ChatBotClient
import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.ninjasquad.springmockk.MockkBean
import com.proto.chatbot.ChatbotResponse
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatBotServiceTest {

	@MockkBean
	private lateinit var chatBotClient: ChatBotClient

	private lateinit var chatbotService: ChatBotService

	private lateinit var expectedResponse: ChatbotResponse

	@BeforeAll
	fun setUp() {
		chatbotService = ChatBotService(chatBotClient)
		expectedResponse = ChatbotResponse.newBuilder()
			.setAnswer("Hello")
			.build()
	}

	@Test
	fun `should return response for slack event`() {
		coEvery { chatBotClient.chat(any<Event>()) } returns expectedResponse

		val testEvent = Event("test", null, "Hi", "test_user", "test_channel")
		val botResponse = runBlocking { chatbotService.chat(testEvent) }

		assert(expectedResponse == botResponse)
	}
}
