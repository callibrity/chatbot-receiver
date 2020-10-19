package misc

import (
	"chatbot-receiver/handler/contract/request"
	"chatbot-receiver/handler/contract/response"
	"chatbot-receiver/service/chatbot"
	"context"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"net/http"
)

func New(s chatbot.Service, logger *zap.Logger) Handler {
	return &handler{
		chatbotService: s,
		logger:         logger,
	}
}

type Handler interface {
	SimpleChat(*gin.Context)
	HeartBeat(*gin.Context)
}

type handler struct {
	chatbotService chatbot.Service
	logger         *zap.Logger
}

// SimpleChat godoc
// @Summary Chat with bot
// @Description start a simple conversation with bot
// @Accept	json
// @Produce	json
// @Param message body request.SimpleChatRequest true "Simple chat request"
// @Success 200 {object} response.ApiResponse
// @Failure 400 {object} response.ApiResponse
// @Failure 500 {object} response.ApiResponse
// @Router /api/test/chatMessage [post]
func (h handler) SimpleChat(ctx *gin.Context) {
	svcCtx := context.WithValue(context.Background(), "clientIP", ctx.ClientIP())

	var req request.SimpleChatRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, response.ApiResponse{
			Error: &response.ApiError{
				Code:    http.StatusBadRequest,
				Message: err.Error(),
			},
		})
	} else if reply, err := h.chatbotService.SimpleChat(
		svcCtx,
		req.Message,
	); err != nil {
		ctx.JSON(http.StatusInternalServerError, response.ApiResponse{
			Error: &response.ApiError{
				Code:    http.StatusInternalServerError,
				Message: err.Error(),
			},
		})
	} else {
		ctx.JSON(http.StatusOK, response.ApiResponse{
			Data: gin.H{
				"answer": reply,
			},
		})
	}
}

// HeartBeat godoc
// @Summary Send heartBeat to bot
// @Description send a heartbeat to bot to check bot health
// @Produce	json
// @Success 200 {object} response.ApiResponse
// @Failure 500 {object} response.ApiResponse
// @Router /api/health [get]
func (h handler) HeartBeat(ctx *gin.Context) {
	svcCtx := context.WithValue(context.Background(), "clientIP", ctx.ClientIP())

	if res, err := h.chatbotService.HeartBeat(svcCtx); err != nil {
		ctx.JSON(http.StatusInternalServerError, response.ApiResponse{
			Error: &response.ApiError{
				Code:    http.StatusInternalServerError,
				Message: err.Error(),
			},
		})
	} else {
		ctx.JSON(http.StatusOK, response.ApiResponse{
			Data: res,
		})
	}
}
