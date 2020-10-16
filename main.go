package main

import (
	"chatbot-receiver/cli"
	"chatbot-receiver/config"
	"chatbot-receiver/handler"
	"chatbot-receiver/middleware"
	"context"
	"fmt"
	"github.com/gin-gonic/gin"
	"io"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	_ "chatbot-receiver/docs"
)

func createServer(appConfig *config.Config) (*http.Server, []io.Closer) {
	var (
		engine      = gin.New()
		routerGroup = engine.Group(appConfig.Server.BasePath)
	)

	middleware.Apply(engine)
	closers := handler.Apply(routerGroup, appConfig)

	server := &http.Server{
		Addr:    ":" + appConfig.Server.Port,
		Handler: engine,
	}

	return server, closers
}

func awaitShutDown(server *http.Server, closers []io.Closer) {
	// Wait for interrupt signal to gracefully shutdown the server with
	// a timeout of 5 seconds.
	quit := make(chan os.Signal)

	// kill (no param) default send syscall.SIGTERM
	// kill -2 is syscall.SIGINT
	// kill -9 is syscall.SIGKILL but can't be catch, so don't need add it
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	// release resources
	for _, closer := range closers {
		if err := closer.Close(); err != nil {
			log.Printf("Error %s on closing %v", err, closer)
		}
	}

	log.Println("Shutting down server...")

	// The context is used to inform the server it has 5 seconds to finish
	// the request it is currently handling
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Fatal("Server forced to shutdown:", err)
	}

	log.Println("Server exited")
}

func run() {
	activeProfile := cli.ProcessCmdOptions()
	server, closers := createServer(config.New(activeProfile))

	// Initializing the server in a goroutine so that
	// it won't block the graceful shutdown handling below
	go func() {
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			panic(fmt.Errorf("listen error: %s", err))
		}
	}()

	awaitShutDown(server, closers)
}

// @title Swagger Chatbot API
// @version 1.0
// @description This is a simple swagger api to interact with chatbot.
// @termsOfService http://swagger.io/terms/

// @contact.name API Support
// @contact.url http://www.swagger.io/support
// @contact.email support@swagger.io

// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html

// @BasePath /chatbot
func main() {
	run()
}
