package middleware

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"time"
)

func Apply(server *gin.Engine) {
	server.Use(
		gin.Recovery(),
		CorrelationIdInterceptor(),
		RequestLogger(),
	)
}

const CorrelationIdKey = "X-Correlation-Id"

func CorrelationIdInterceptor() gin.HandlerFunc {
	return func(context *gin.Context) {
		corrId := context.GetHeader(CorrelationIdKey)
		if corrId == "" {
			corrId := uuid.New().String()
			context.Request.Header.Add(CorrelationIdKey, corrId)
		}
	}
}

func RequestLogger() gin.HandlerFunc {
	return gin.LoggerWithFormatter(func(params gin.LogFormatterParams) string {
		return fmt.Sprintf(
			"%s - [%s] %s %s %s %d %s\n",
			params.ClientIP,
			params.TimeStamp.Format(time.RFC822),
			params.Request.Header.Get(CorrelationIdKey),
			params.Method,
			params.Path,
			params.StatusCode,
			params.Latency,
		)
	})
}
