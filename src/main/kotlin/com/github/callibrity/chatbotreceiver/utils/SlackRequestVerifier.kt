package com.github.callibrity.chatbotreceiver.utils

import com.github.callibrity.chatbotreceiver.config.SlackConfiguration
import org.springframework.stereotype.Component
import com.google.common.hash.Hashing

@Component
class SlackRequestVerifier(
    private val slackConfiguration: SlackConfiguration
) {
    fun isVerifiedSlackRequest(
        slackSignature: String,
        slackRequest_timestamp: String,
        reqBody: ByteArray
    ): Boolean {
        val baseStr = "v0:$slackRequest_timestamp:".toByteArray() + reqBody
        val hashFunc = Hashing.hmacSha256(slackConfiguration.signingSecret.toByteArray())
        val computedHash = "v0=" + hashFunc.hashBytes(baseStr).toString()

        return computedHash == slackSignature
    }
}
