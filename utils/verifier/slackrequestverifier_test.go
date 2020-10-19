package verifier

import (
	"bytes"
	"testing"
)

const testSecret = "8f742231b10e8888abcd99yyyzzz85a5"

func TestNew(t *testing.T) {
	tests := []struct {
		secret string
		v      *verifier
	}{
		{
			testSecret,
			&verifier{
				secret: []byte(testSecret),
			},
		},
	}

	for _, test := range tests {
		if actual, _ := New(test.secret).(*verifier); !bytes.Equal(actual.secret, test.v.secret) {
			t.Errorf(
				"New(%s); got %+v; expected %+v",
				test.secret, actual, test.v,
			)
		}
	}
}

func TestVerifier_VerifyRequest(t *testing.T) {
	buf := bytes.Buffer{}
	buf.WriteString("token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&")
	buf.WriteString("channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&")
	buf.WriteString("command=%2Fwebhook-collect&text=&response_url=")
	buf.WriteString("https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&")
	buf.WriteString("trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c")

	tests := []struct {
		slackSignature, slackRequestTimestamp string
		reqBody                               []byte
		expected                              bool
	}{
		{
			"v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503",
			"1531420618",
			[]byte(buf.Bytes()),
			true,
		},
	}

	v := New(testSecret)
	for _, test := range tests {
		if actual, err := v.VerifyRequest(
			test.slackSignature,
			test.slackRequestTimestamp,
			test.reqBody,
		); err != nil {
			t.Errorf(
				"VerifyRequest(%s, %s, %s); got error: %v; expected: %t",
				test.slackSignature, test.slackRequestTimestamp, test.reqBody, err, test.expected,
			)
		} else if actual != test.expected {
			t.Errorf(
				"VerifyRequest(%s, %s, %s); got: %v; expected: %t",
				test.slackSignature, test.slackRequestTimestamp, test.reqBody, err, test.expected,
			)
		}
	}
}
