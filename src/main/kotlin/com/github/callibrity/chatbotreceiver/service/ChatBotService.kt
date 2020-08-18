package com.github.callibrity.chatbotreceiver.service

import com.github.callibrity.chatbotreceiver.grpc.client.ChatBotClient
import com.github.callibrity.chatbotreceiver.request.slack.Event
import com.proto.chatbot.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ChatBotService(
    private val chatBotClient: ChatBotClient
) {

    @PostConstruct
    private fun checkService() = runBlocking {
        chatBotClient.heartBeat()
    }

    suspend fun chat(event: Event): ChatbotResponse =
      chatBotClient.chat(event)

    suspend fun chat(message: String): ChatbotResponse =
      chatBotClient.chat(message)

    suspend fun heartBeat(): String =
      chatBotClient.heartBeat()
}
