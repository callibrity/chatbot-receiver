package com.github.callibrity.chatbotreceiver.config

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@ConfigurationProperties("grpc.chatbot-service-properties")
class ChatbotServiceProperties {
    lateinit var host: String
    lateinit var port: String
}

@Configuration
class GrpcConfiguration(
    private val chatbotServiceProperties: ChatbotServiceProperties
) {

    lateinit var channelForChatbotService: ManagedChannel

    @PostConstruct
    fun initializeChannel() {
        println("Invoked")
        channelForChatbotService = ManagedChannelBuilder
            .forAddress(chatbotServiceProperties.host, chatbotServiceProperties.port.toInt())
            .usePlaintext()
            .build()
    }

    @PreDestroy
    fun shutDownChannel() {
        println("Shutting channel down")
        channelForChatbotService.shutdown()
    }
}
