package com.github.callibrity.chatbotreceiver.service.grpc

import com.github.callibrity.chatbotreceiver.config.GrpcConfiguration
import com.proto.chatbot.ChatbotRequest
import com.proto.chatbot.ChatbotResponse
import com.proto.chatbot.ChatbotServiceGrpc
import org.springframework.stereotype.Service

@Service
class ChatBotService(
    grpcConfiguration: GrpcConfiguration
) {
    private val chatBotClient = ChatbotServiceGrpc
        .newBlockingStub(grpcConfiguration.channelForChatbotService)

    private val requestBuilder = ChatbotRequest.newBuilder()

    fun chat(message: String, user: String, channel: String): ChatbotResponse = requestBuilder
        .setQuestion(message)
        .setUser(user)
        .setChannel(channel)
        .build()
        .run { chatBotClient.chat(this) }
        .apply {
            println("responded with: $answer")
        }
}
