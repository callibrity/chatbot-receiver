package slack

import (
	"chatbot-receiver/common/logger"
	"chatbot-receiver/handler/common/testutils"
	"chatbot-receiver/handler/contract/request"
	"encoding/json"
	"errors"
	"github.com/gin-gonic/gin"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

type mockSlackRequestVerifier struct{}

func (mockSlackRequestVerifier) VerifyRequest(
	_, _ string,
	reqBody []byte,
) (bool, error) {
	var botEventReq request.SlackBotEventRequest
	json.Unmarshal(reqBody, &botEventReq)

	if botEventReq.Event.Text == "" {
		return false, errors.New("empty request body")
	}

	if botEventReq.Event.Text == "false" {
		return false, nil
	}

	return true, nil
}

func createTestRequest(body string) *http.Request {
	body = strings.TrimSpace(body)
	req, err := http.NewRequest(
		"POST",
		"/test",
		strings.NewReader(body),
	)

	if err != nil {
		panic(err)
	}

	return req
}

func TestHandler_Chat(t *testing.T) {
	tests := []struct {
		name string
		req  *http.Request
		code int
		res  string
	}{
		{
			name: "should return challenge from slack url verification request",
			req: createTestRequest(`{
				"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
				"challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
				"type": "url_verification"
			}`),
			code: http.StatusOK,
			res:  "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P",
		},
		{
			name: "should return 400 bad request for event callback request with empty body",
			req: createTestRequest(`{
				"token": "ZZZZZZWSxiZZZ2yIvs3peJ",
				"team_id": "test team id",
				"api_app_id": "test app id",
				"event": {
					"type": "app_mention",
					"user": "test",
					"text": "",
					"ts": "1515449438.000011",
					"channel": "test",
					"event_ts": "1515449438000011"
				},
				"type": "event_callback",
				"event_id": "test id",
				"event_time": 1515449438000011,
				"authed_users": [
					"test"
				]
			}`),
			code: http.StatusBadRequest,
			res:  `{"error":"empty request body"}`,
		},
		{
			name: "should return 400 bad request for event callback request that can't be verified",
			req: createTestRequest(`{
				"token": "ZZZZZZWSxiZZZ2yIvs3peJ",
				"team_id": "test team id",
				"api_app_id": "test app id",
				"event": {
					"type": "app_mention",
					"user": "test",
					"text": "false",
					"ts": "1515449438.000011",
					"channel": "test",
					"event_ts": "1515449438000011"
				},
				"type": "event_callback",
				"event_id": "test id",
				"event_time": 1515449438000011,
				"authed_users": [
					"test"
				]
			}`),
			code: http.StatusBadRequest,
			res:  `{"error":"Request is not verified"}`,
		},
		{
			name: "should return 200 ok for event callback request that is valid",
			req: createTestRequest(`{
				"token": "ZZZZZZWSxiZZZ2yIvs3peJ",
				"team_id": "test team id",
				"api_app_id": "test app id",
				"event": {
					"type": "app_mention",
					"user": "test",
					"text": "Hello",
					"ts": "1515449438.000011",
					"channel": "test",
					"event_ts": "1515449438000011"
				},
				"type": "event_callback",
				"event_id": "test id",
				"event_time": 1515449438000011,
				"authed_users": [
					"test"
				]
			}`),
			code: http.StatusOK,
			res:  `{"answer":"test"}`,
		},
	}

	server := gin.Default()
	handler := New(mockSlackRequestVerifier{}, testutils.MockChatbotService{}, logger.New("INFO"))

	server.POST("/test", handler.Chat)
	for _, test := range tests {
		t.Run(test.name, func(tt *testing.T) {
			w := httptest.NewRecorder()
			server.ServeHTTP(w, test.req)

			if w.Code != test.code {
				tt.Errorf(
					"Chat(_); got: %d; expected: %d",
					w.Code, test.code,
				)
			}

			res := strings.Trim(w.Body.String(), "\"")
			if res != test.res || !strings.Contains(res, test.res) {
				tt.Errorf(
					"Chat(_); got: %s; expected: %s",
					res, test.res,
				)
			}
		})
	}
}
