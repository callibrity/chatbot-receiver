package chatbot

import (
	"chatbot-receiver/generated/chatbot"
	"chatbot-receiver/handler/contract/request"
	"context"
	"fmt"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"math/rand"
	"regexp"
	"time"
)

func New(conn *grpc.ClientConn, timeout int8, logger *zap.Logger) Service {
	return service{
		chatbotClient: chatbot.NewChatbotServiceClient(conn),
		usernameRegex: regexp.MustCompile(`<[@a-zA-Z0-9]*>`),
		timeout:       time.Duration(timeout) * time.Second,
		logger:        logger,
	}
}

type Service interface {
	HeartBeat(context.Context) (int32, error)
	SimpleChat(context.Context, string) (string, error)
	Chat(context.Context, *request.BotEvent) (string, error)
}

type service struct {
	chatbotClient chatbot.ChatbotServiceClient
	usernameRegex *regexp.Regexp
	timeout       time.Duration
	logger        *zap.Logger
}

func (s service) HeartBeat(ctx context.Context) (int32, error) {
	ctx, cancel := context.WithTimeout(ctx, s.timeout)
	defer cancel()

	req := chatbot.HeartBeat{
		Number: rand.Int31(),
	}

	s.logger.Info("Heartbeat", zap.Int32("Number", req.Number))

	var resp *chatbot.HeartBeat
	err := s.grpcDo(ctx, &req, func(response interface{}, err error) error {
		if err != nil {
			return err
		}

		if v, ok := response.(*chatbot.HeartBeat); ok {
			resp = v
		} else {
			return fmt.Errorf("invalid response: %v", response)
		}

		return nil
	})

	var num int32
	if resp != nil {
		num = resp.GetNumber()
	}

	return num, err
}

func (s service) SimpleChat(ctx context.Context, msg string) (string, error) {
	ctx, cancel := context.WithTimeout(ctx, s.timeout)
	defer cancel()

	req := chatbot.ChatbotRequest{
		Question: msg,
	}

	s.logger.Info("Sending request", zap.Any("msg", msg))

	return s.doChat(ctx, &req)
}

func (s service) Chat(ctx context.Context, botEvent *request.BotEvent) (string, error) {
	ctx, cancel := context.WithTimeout(ctx, s.timeout)
	defer cancel()

	req := chatbot.ChatbotRequest{
		Question: s.usernameRegex.ReplaceAllString(botEvent.Text, ""),
		User:     botEvent.User,
		Channel:  botEvent.Channel,
	}

	s.logger.Info("Sending request", zap.Any("botEvent", botEvent))

	return s.doChat(ctx, &req)
}

func (s service) doChat(ctx context.Context, req *chatbot.ChatbotRequest) (string, error) {
	var resp *chatbot.ChatbotResponse
	err := s.grpcDo(ctx, req, func(response interface{}, err error) error {
		if err != nil {
			return err
		}

		if v, ok := response.(*chatbot.ChatbotResponse); ok {
			resp = v
		} else {
			return fmt.Errorf("invalid response: %v", response)
		}

		return nil
	})

	var answer string
	if resp != nil {
		answer = resp.Answer
	}

	return answer, err
}

func (s service) grpcDo(
	ctx context.Context,
	req interface{},
	callback func(resp interface{}, err error) error,
) error {
	rpcError := make(chan error, 1)
	go func() {
		switch v := req.(type) {
		case *chatbot.HeartBeat:
			rpcError <- callback(s.chatbotClient.HeartBeat(ctx, v))
		case *chatbot.ChatbotRequest:
			rpcError <- callback(s.chatbotClient.Chat(ctx, v))
		default:
			rpcError <- fmt.Errorf("invalid request: %v", req)
		}
	}()

	select {
	case <-ctx.Done():
		// Wait for callback to return
		<-rpcError
		return ctx.Err()
	case err := <-rpcError:
		return err
	}
}
