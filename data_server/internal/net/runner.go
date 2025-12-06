package net

import (
	"data_server/internal/config"
	"data_server/internal/log"
	"data_server/internal/net/env"
	"data_server/internal/service"

	"github.com/gin-gonic/gin"
)

func StartServer(cfg *config.Config) {
	svc := service.ServiceInit(cfg)
	AddRoutes(svc)

	switch cfg.Env {
	case "local":
		log.WithField("mode", gin.Mode()).WithField("port", cfg.Port).Info("starting local server")
		env.RunLocal(svc)
	case "lambda":
		log.WithField("mode", gin.Mode()).Info("starting lambda function")
		env.RunLambda(svc)
	default:
		log.WithField("env", cfg.Env).Fatal("Invalid runtime ENV. Terminating.")
	}
}
