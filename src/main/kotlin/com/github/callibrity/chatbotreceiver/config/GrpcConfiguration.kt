package com.github.callibrity.chatbotreceiver.config

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@ConfigurationProperties("grpc.chatbot-service-properties")
class ChatbotServiceProperties {
    lateinit var host: String
    lateinit var port: String
}

@Configuration
@ConfigurationProperties("grpc.chatbot-client")
class ChatbotClientProperties {
    lateinit var threadpool: String
}

@Configuration
class GrpcConfiguration(
    private val chatbotServiceProperties: ChatbotServiceProperties,
    private val chatbotClientProperties: ChatbotClientProperties
) {

    private val logger = LoggerFactory.getLogger(GrpcConfiguration::class.java)

    private var dispatcher: ExecutorCoroutineDispatcher = Executors
      .newFixedThreadPool(chatbotClientProperties.threadpool.toInt())
      .asCoroutineDispatcher()

    var channel: ManagedChannel = ManagedChannelBuilder
      .forAddress(
        chatbotServiceProperties.host,
        chatbotServiceProperties.port.toInt()
      )
      .usePlaintext()
      .executor(dispatcher.asExecutor())
      .build()

    @PreDestroy
    fun close() {
        logger.info("Shutting down service channel...")
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        logger.info("service channel shut down")

        logger.info("Closing coroutine dispatcher...")
        dispatcher.close()
        logger.info("coroutine dispatcher closed")
    }
}
