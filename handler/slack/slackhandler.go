package slack

import (
	"chatbot-receiver/handler/contract/request"
	"chatbot-receiver/service/chatbot"
	"chatbot-receiver/utils/verifier"
	"context"
	"encoding/json"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"net/http"
)

func New(v verifier.SlackRequestVerifier, s chatbot.Service, logger *zap.Logger) Handler {
	return handler{
		verifier:       v,
		chatbotService: s,
		logger:         logger,
	}
}

type Handler interface {
	Chat(ctx *gin.Context)
}

type handler struct {
	verifier       verifier.SlackRequestVerifier
	chatbotService chatbot.Service
	logger         *zap.Logger
}

func (h handler) Chat(ctx *gin.Context) {
	if body, err := ctx.GetRawData(); err != nil {
		ctx.JSON(http.StatusInternalServerError, err)
	} else {
		var req request.SlackBotEventRequest
		if err = json.Unmarshal(body, &req); err != nil {
			ctx.JSON(http.StatusBadRequest, err)
		} else {
			h.handleBotRequest(ctx, &req, body)
		}
	}
}

func (h handler) handleBotRequest(
	ctx *gin.Context,
	req *request.SlackBotEventRequest,
	body []byte,
) {
	switch req.Type {
	case "url_verification":
		ctx.JSON(http.StatusOK, req.Challenge)
	case "event_callback":
		svcCtx := context.WithValue(context.Background(), "clientIP", ctx.ClientIP())

		if isVerified, err := h.verifier.VerifyRequest(
			ctx.GetHeader("x-slack-signature"),
			ctx.GetHeader("x-slack-request-timestamp"),
			body,
		); err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"error": err.Error(),
			})
			h.logger.Info("Error on verifying request", zap.Error(err))
		} else if !isVerified {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"error": "Request is not verified",
			})
			h.logger.Info("Request is not verified")
		} else if resp, err := h.chatbotService.Chat(
			svcCtx,
			&req.Event,
		); err != nil {
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"error": err.Error(),
			})
			h.logger.Info("Error on chatbotService.Chat(_)", zap.Any("event", req.Event))
		} else {
			ctx.JSON(http.StatusOK, gin.H{
				"answer": resp,
			})
			h.logger.Info(
				"Received response from chatbotService.Chat(_)",
				zap.String("answer", resp),
			)
		}
	default:
		ctx.JSON(http.StatusNotImplemented, "Not Implemented")
		h.logger.Info("Unhandled request type", zap.String("requestType", req.Type))
	}
}
