package com.github.callibrity.chatbotreceiver.service

import com.github.callibrity.chatbotreceiver.config.GrpcConfiguration
import com.github.callibrity.chatbotreceiver.grpc.client.ChatBotClient
import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.proto.chatbot.*
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ChatBotService(
    grpcConfiguration: GrpcConfiguration
) {
    private val chatBotClient = ChatBotClient(
      grpcConfiguration.channel
    )

    @PostConstruct
    private fun checkService() {
        chatBotClient.heartBeat()
    }

    fun chat(event: Event): ChatbotResponse =
      chatBotClient.chat(event)

    fun chat(message: String): ChatbotResponse =
      chatBotClient.chat(message)

    fun heartBeat(): String =
      chatBotClient.heartBeat()
}
