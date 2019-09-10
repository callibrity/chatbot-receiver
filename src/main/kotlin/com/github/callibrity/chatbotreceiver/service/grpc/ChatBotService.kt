package com.github.callibrity.chatbotreceiver.service.grpc

import com.github.callibrity.chatbotreceiver.config.GrpcConfiguration
import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.proto.chatbot.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatBotService(
    grpcConfiguration: GrpcConfiguration
) {
    private val chatBotClient = ChatbotServiceGrpc
        .newBlockingStub(grpcConfiguration.channelForChatbotService)

    private val chatbotRequestBuilder = ChatbotRequest.newBuilder()
    private val heartBeatRequestBuilder = HeartBeat.newBuilder()

    private val logger = LoggerFactory.getLogger(ChatBotService::class.java)

    fun chat(event: Event): ChatbotResponse = chatbotRequestBuilder
        .also { logger.info("User: ${event.user}, Channel: ${event.channel}") }
        .setQuestion(event.text)
        .setUser(event.user)
        .setChannel(event.channel)
        .build()
        .run { chatBotClient.chat(this) }
        .apply { logger.info("Bot responded with: $answer") }

    fun chat(message: String): ChatbotResponse = chatbotRequestBuilder
        .also { logger.info("Chat message: $message") }
        .setQuestion(message)
        .build()
        .run { chatBotClient.chat(this) }
        .apply { logger.info("Bot responded with: $answer") }

    fun heartBeat(): String {
        val randomNumber =  (Int.MIN_VALUE..Int.MAX_VALUE).random()
        val heartBeat = heartBeatRequestBuilder.setNumber(randomNumber).build()

        return try {
            val responseHeartBeat = chatBotClient.heartBeat(heartBeat)

            if (randomNumber == responseHeartBeat.number) {
                "Bot is alive".also { logger.info(it) }
            } else {
                "Bot is alive, but something is wrong.".also { logger.warn(it) }
            }
        } catch (ex: StatusRuntimeException) {
            if (ex.status == Status.INTERNAL) {
                "Something is very broken, bot is down.".also { logger.error(it) }
            } else {
                "Bot might be alive, try again later.".also { logger.error(it) }
            }
        }
    }
}
