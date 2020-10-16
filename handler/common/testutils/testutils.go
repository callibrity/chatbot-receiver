package testutils

import (
	"chatbot-receiver/handler/contract/request"
	"context"
	"errors"
)

type MockChatbotService struct{}

func (MockChatbotService) HeartBeat(ctx context.Context) (int32, error) {
	if ctx.Value("clientIP") == "" {
		return -1, errors.New("something went wrong")
	}
	return 42, nil
}

func (MockChatbotService) SimpleChat(_ context.Context, msg string) (string, error) {
	if msg == "" {
		return "", errors.New("invalid request")
	}

	return "test", nil
}

func (MockChatbotService) Chat(_ context.Context, req *request.BotEvent) (string, error) {
	if req == nil {
		return "", errors.New("invalid request")
	}

	return "test", nil
}
