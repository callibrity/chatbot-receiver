package handler

import (
	"chatbot-receiver/common/logger"
	"chatbot-receiver/config"
	mischandler "chatbot-receiver/handler/misc"
	slackhandler "chatbot-receiver/handler/slack"
	chatbotservice "chatbot-receiver/service/chatbot"
	"chatbot-receiver/utils/verifier"
	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"io"
)

func Apply(routerGroup *gin.RouterGroup, appConfig *config.Config) []io.Closer {
	var (
		_logger						= logger.New(appConfig.Logging.Level)
		slackRequestVerifier 		= verifier.New(appConfig.Slack.SigningSecret)
		chatbotServiceProperties	= appConfig.Grpc.ChatBotServiceProperties
		grpcClientConn				= initializeGrpcClientConn(
			chatbotServiceProperties.Host + ":" + chatbotServiceProperties.Port,
			_logger,
		)
		chatbotService				= chatbotservice.New(
			grpcClientConn,
			chatbotServiceProperties.Timeout,
			_logger,
		)
		slackHandler 				= slackhandler.New(
			slackRequestVerifier,
			chatbotService,
			_logger,
		)
		miscHandler					= mischandler.New(
			chatbotService,
			_logger,
		)
	)

	routerGroup.POST("/api/chatMessage", slackHandler.Chat)
	routerGroup.POST("/api/test/chatMessage", miscHandler.SimpleChat)
	routerGroup.GET("/api/health", miscHandler.HeartBeat)

	closers := []io.Closer{grpcClientConn}

	initializeSwagger(routerGroup, appConfig)

	return closers
}

func initializeGrpcClientConn(target string, logger *zap.Logger) *grpc.ClientConn {
	var options []grpc.DialOption

	options = append(options, grpc.WithInsecure())
	conn, err := grpc.Dial(target, options...)

	if err != nil {
		logger.Fatal("Fail to dial: %s", zap.Error(err))
	}

	return conn
}

func initializeSwagger(router *gin.RouterGroup, config *config.Config) {
	router.GET(
		"/swagger/*any",
		ginSwagger.WrapHandler(swaggerFiles.Handler),
	)
}
