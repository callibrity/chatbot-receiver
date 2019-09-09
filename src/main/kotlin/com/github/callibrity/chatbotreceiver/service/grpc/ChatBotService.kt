package com.github.callibrity.chatbotreceiver.service.grpc

import com.github.callibrity.chatbotreceiver.config.GrpcConfiguration
import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.proto.chatbot.ChatbotRequest
import com.proto.chatbot.ChatbotResponse
import com.proto.chatbot.ChatbotServiceGrpc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatBotService(
    grpcConfiguration: GrpcConfiguration
) {
    private val chatBotClient = ChatbotServiceGrpc
        .newBlockingStub(grpcConfiguration.channelForChatbotService)

    private val requestBuilder = ChatbotRequest.newBuilder()

    private val logger = LoggerFactory.getLogger(ChatBotService::class.java)

    fun chat(event: Event): ChatbotResponse = requestBuilder
        .also { logger.info("User: ${event.user}, Channel: ${event.channel}") }
        .setQuestion(event.text)
        .setUser(event.user)
        .setChannel(event.channel)
        .build()
        .run { chatBotClient.chat(this) }
        .apply { logger.info("Bot responded with: $answer") }
}
