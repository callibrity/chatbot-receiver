package com.github.callibrity.chatbotreceiver.grpc.client

import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.proto.chatbot.ChatbotRequest
import com.proto.chatbot.ChatbotResponse
import com.proto.chatbot.ChatbotServiceGrpcKt
import com.proto.chatbot.HeartBeat
import io.grpc.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ChatBotClient(
	val channel: ManagedChannel
) {
	private val logger = LoggerFactory.getLogger(ChatBotClient::class.java)
	private val stub = ChatbotServiceGrpcKt.ChatbotServiceCoroutineStub(channel)
	private val filterRegex = Regex("<[@a-zA-Z0-9]*>")
	private val chatbotRequestBuilder = ChatbotRequest.newBuilder()
	private val heartBeatRequestBuilder = HeartBeat.newBuilder()

	companion object {
		private const val DEADLINE = 6000L
	}

	fun chat(event: Event): ChatbotResponse = runBlocking {
		chatbotRequestBuilder
			.also { logger.info("User: ${event.user}, Channel: ${event.channel}") }
			.setQuestion(filterRegex.replace(event.text ?: "Hi.", ""))
			.setUser(event.user)
			.setChannel(event.channel)
			.build()
			.let { request ->
				stub.withDeadline(
					Deadline.after(DEADLINE, TimeUnit.SECONDS)
				).chat(request)
			}
			.apply { logger.info("Bot responded with: $answer") }
	}

	fun chat(msg: String): ChatbotResponse = runBlocking {
		chatbotRequestBuilder
			.also { logger.info("Chat message: $msg") }
			.setQuestion(msg)
			.build()
			.let { request ->
				stub.withDeadline(
					Deadline.after(DEADLINE, TimeUnit.SECONDS)
				).chat(request)
			}
			.apply { logger.info("Bot responded with: $answer") }
	}

	fun heartBeat(): String = runBlocking {
		val randomNumber =  (Int.MIN_VALUE..Int.MAX_VALUE).random()
		val heartBeat = heartBeatRequestBuilder.setNumber(randomNumber).build()

		try {
			val responseHeartBeat = stub.withDeadline(
				Deadline.after(DEADLINE + 5000, TimeUnit.SECONDS)
			).heartBeat(heartBeat)

			"Bot is alive: ${responseHeartBeat.number}"
				.also { logger.info(it) }
		} catch (ex: StatusException) {
			when (ex.status) {
				Status.INTERNAL ->
					"Something is broken, bot is down.".also {
						logger.error(ex.message)
						logger.error(it, ex.cause)
					}
				Status.DEADLINE_EXCEEDED ->
					"Deadline exceeded, try again later.".also {
						logger.error(ex.message)
					}
				else ->
					"Something is wrong, please diagnose".also {
						logger.error(ex.message)
						logger.error(it, ex.cause)
					}
			}
		}
	}
}
