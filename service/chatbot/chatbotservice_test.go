package chatbot

import (
	"chatbot-receiver/common/logger"
	"chatbot-receiver/generated/chatbot"
	"chatbot-receiver/handler/contract/request"
	"context"
	"errors"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/grpc/test/bufconn"
	"log"
	"net"
	"testing"
)

type mockGrpcChatbotService struct {
	chatbot.UnimplementedChatbotServiceServer
}

func (*mockGrpcChatbotService) HeartBeat(ctx context.Context, req *chatbot.HeartBeat) (*chatbot.HeartBeat, error) {
	return &chatbot.HeartBeat{
		Number: req.GetNumber(),
	}, nil
}

func (*mockGrpcChatbotService) Chat(ctx context.Context, req *chatbot.ChatbotRequest) (*chatbot.ChatbotResponse, error) {
	if req.GetQuestion() == "" || req.GetUser() == "" || req.GetChannel() == "" {
		return nil, status.Errorf(codes.InvalidArgument, "Invalid request %+v", req)
	}

	return &chatbot.ChatbotResponse{
		Answer: "Hello",
	}, nil
}

func dialer() func(context.Context, string) (net.Conn, error) {
	var (
		listener = bufconn.Listen(1024 * 1024)
		server   = grpc.NewServer()
	)

	chatbot.RegisterChatbotServiceServer(server, &mockGrpcChatbotService{})

	go func() {
		if err := server.Serve(listener); err != nil {
			log.Fatal(err)
		}
	}()

	return func(context.Context, string) (net.Conn, error) {
		return listener.Dial()
	}
}

func TestService_HeartBeat(t *testing.T) {
	tests := []struct {
		name string
	}{
		{
			"should return number back",
		},
	}

	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "", grpc.WithInsecure(), grpc.WithContextDialer(dialer()))
	if err != nil {
		log.Fatal(err)
	}

	botService := New(conn, 1, logger.New("INFO"))
	for _, test := range tests {
		t.Run(test.name, func(tt *testing.T) {
			_, err := botService.HeartBeat(context.Background())

			if err != nil {
				tt.Errorf("HeartBeat(_); got error %v; expected no error", err)
			}
		})
	}
}

func TestService_Chat(t *testing.T) {
	tests := []struct {
		name     string
		botEvent *request.BotEvent
		res      string
		err      error
	}{
		{
			"should return invalid request error",
			&request.BotEvent{},
			"",
			status.Errorf(codes.InvalidArgument, "Invalid request %+v", &chatbot.ChatbotRequest{}),
		},
		{
			"should return valid response",
			&request.BotEvent{
				Text:    "test",
				Channel: "test",
				User:    "@test",
			},
			"Hello",
			nil,
		},
	}

	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "", grpc.WithInsecure(), grpc.WithContextDialer(dialer()))
	if err != nil {
		log.Fatal(err)
	}

	botService := New(conn, 1, logger.New("INFO"))
	for _, test := range tests {
		t.Run(test.name, func(tt *testing.T) {
			res, err := botService.Chat(context.Background(), test.botEvent)

			if res != test.res {
				tt.Errorf(
					"Chat(_, %+v); got %s; expected %s",
					test.botEvent, res, test.res,
				)
			}

			if err != nil && !errors.Is(err, test.err) {
				tt.Errorf(
					"Chat(_, %+v); got error %v; expected error %v",
					test.botEvent, err, test.err,
				)
			}
		})
	}
}
