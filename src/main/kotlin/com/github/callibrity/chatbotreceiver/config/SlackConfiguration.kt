package com.github.callibrity.chatbotreceiver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("slack")
class SlackConfiguration {
    lateinit var signingSecret: String
    lateinit var botUserOauthAccesToken: String
}
