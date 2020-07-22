package com.github.callibrity.chatbotreceiver.utils

import com.github.callibrity.chatbotreceiver.config.SlackConfiguration
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SlackRequestVerifierTest {

	@MockkBean
	private lateinit var slackConfiguration: SlackConfiguration

	private lateinit var slackRequestVerifier: SlackRequestVerifier

	@BeforeAll
	fun setUp() {
		slackRequestVerifier = SlackRequestVerifier(slackConfiguration)
	}

	@Test
	fun `should return true for valid request`() {
		every { slackConfiguration.signingSecret } returns "8f742231b10e8888abcd99yyyzzz85a5"

		val expectedHash = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"
		val timeStamp = "1531420618"
		val reqBody = ("token=xyzz0WbapA4vBCDEFasx0q6G&" +
			"team_id=T1DC2JH3J&" +
			"team_domain=testteamnow&" +
			"channel_id=G8PSS9T3V&" +
			"channel_name=foobar&" +
			"user_id=U2CERLKJA&" +
			"user_name=roadrunner&" +
			"command=%2Fwebhook-collect&" +
			"text=&response_url=" +
			"https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&" +
			"trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c").toByteArray()

		val result = slackRequestVerifier.isVerifiedSlackRequest(
			expectedHash,
			timeStamp,
			reqBody
		)

		assert(result)
	}

	@Test
	fun `should return true for invalid request`() {
		every { slackConfiguration.signingSecret } returns "8f742231b10e8888abcd99yyyzzz85a5"

		val expectedHash = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"
		val timeStamp = "1531420618"
		val reqBody = ("token=xyzz0WbapA4vBCDEFasx0q6G&" +
			"team_id=test_id&" +
			"team_domain=test&" +
			"channel_id=G8PSS9T3V&" +
			"channel_name=foobar&" +
			"user_id=U2CERLKJA&" +
			"user_name=roadrunner&" +
			"command=%2Fwebhook-collect&" +
			"text=&response_url=" +
			"https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&" +
			"trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c").toByteArray()

		val result = slackRequestVerifier.isVerifiedSlackRequest(
			expectedHash,
			timeStamp,
			reqBody
		)

		assert(!result)
	}
}
