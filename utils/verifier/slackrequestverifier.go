package verifier

import (
	"bytes"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
)

func New(secret string) SlackRequestVerifier {
	return &verifier{
		secret: []byte(secret),
	}
}

type SlackRequestVerifier interface {
	VerifyRequest(
		slackSignature, slackRequestTimestamp string,
		reqBody []byte,
	) (bool, error)
}

type verifier struct {
	secret []byte
}

func (v *verifier) VerifyRequest(
	slackSignature, slackRequestTimestamp string,
	reqBody []byte,
) (bool, error) {
	var buffer bytes.Buffer

	buffer.WriteString("v0:")
	buffer.WriteString(slackRequestTimestamp)
	buffer.WriteString(":")
	buffer.Write(reqBody)

	hash := hmac.New(sha256.New, v.secret)

	if _, err := hash.Write(buffer.Bytes()); err != nil {
		return false, err
	}

	computed := "v0=" + hex.EncodeToString(hash.Sum(nil))

	return computed == slackSignature, nil
}
