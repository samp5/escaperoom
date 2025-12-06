package service

import (
	"data_server/internal/config"
	"data_server/internal/dynamo"
	"data_server/internal/log"
	"data_server/internal/mongodb"
	"data_server/internal/s3"

	"github.com/gin-gonic/gin"
)

var (
	GinMode = "debug"
)

type Service struct {
	Router *gin.Engine
	Cfg    *config.Config
	Dynamo *dynamo.Dynamo
	S3     *s3.S3
	Mongo  *mongodb.MongoDB
}

func ServiceInit(cfg *config.Config) (svc *Service) {
	var err error

	gin.SetMode(GinMode)
	svc = &Service{
		Cfg:    cfg,
		Router: gin.New(),
	}

	svc.Dynamo, err = dynamo.NewDynamo()
	if err != nil {
		log.WithError(err).Fatal("could not initialize dynamo")
	}

	svc.S3, err = s3.NewS3(cfg)
	if err != nil {
		log.WithError(err).Fatal("could not initialize s3")
	}

	svc.Mongo, err = mongodb.NewMongoDB(cfg)
	if err != nil {
		log.WithError(err).Fatal("could not initialize mongodb")
	}

	return svc
}
