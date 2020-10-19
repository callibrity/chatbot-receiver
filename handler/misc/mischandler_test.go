package misc

import (
	"chatbot-receiver/common/logger"
	"chatbot-receiver/handler/common/testutils"
	"github.com/gin-gonic/gin"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func createTestRequest(method, body string) *http.Request {
	body = strings.TrimSpace(body)
	req, err := http.NewRequest(
		method,
		"/test",
		strings.NewReader(body),
	)

	if err != nil {
		panic(err)
	}

	return req
}

func TestHandler_SimpleChat(t *testing.T) {
	tests := []struct {
		name string
		req  *http.Request
		code int
		res  string
	}{
		{
			name: "should return 400 bad request for invalid request data",
			req:  createTestRequest("POST", ""),
			code: http.StatusBadRequest,
			res:  `{"error":{"code":400,"message":"EOF"}}`,
		},
		{
			name: "should return 500 internal server error when service return error",
			req: createTestRequest("POST", `{
				"message":	""
			}`),
			code: http.StatusInternalServerError,
			res:  `{"error":{"code":500,"message":"invalid request"}}`,
		},
		{
			name: "should return 200 ok for valid request",
			req: createTestRequest("POST", `{
				"message":	"hello"
			}`),
			code: http.StatusOK,
			res:  `{"data":{"answer":"test"}}`,
		},
	}

	server := gin.Default()
	handler := New(testutils.MockChatbotService{}, logger.New("INFO"))

	server.POST("/test", handler.SimpleChat)
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

func TestHandler_HeartBeat(t *testing.T) {
	tests := []struct {
		name string
		req  *http.Request
		code int
		res  string
	}{
		{
			name: "should return 500 internal server error when service return error",
			req:  createTestRequest("GET", ""),
			code: http.StatusInternalServerError,
			res:  `{"error":{"code":500,"message":"something went wrong"}}`,
		},
		{
			name: "should return 200 ok for valid request",
			req: func() *http.Request {
				r := createTestRequest("GET", "")
				r.Header.Add("X-Forwarded-For", "127.0.0.1")
				return r
			}(),
			code: http.StatusOK,
			res:  `{"data":42}`,
		},
	}

	server := gin.Default()
	handler := New(testutils.MockChatbotService{}, logger.New("INFO"))

	server.GET("/test", handler.HeartBeat)
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
