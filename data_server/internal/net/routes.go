package net

import (
	"data_server/internal/net/handlers"
	"data_server/internal/service"
)

func AddRoutes(svc *service.Service) {
	svc.Router.NoRoute(handlers.NotFoundHandler)

	// user APIs
	svc.Router.HEAD("/user/:username", handlers.CheckUser(svc))
	svc.Router.POST("/user/:username/:password", handlers.AddUser(svc))
	svc.Router.GET("/user/:username/:password", handlers.Login(svc))

	// map APIs
	mapGroup := svc.Router.Group("/map")
	mapGroup.Use(authMiddleware(svc))

	mapGroup.POST("", handlers.PostMap(svc))
	mapGroup.GET("/:mapid", handlers.GetMap(svc))
	mapGroup.GET("/:mapid/metadata", handlers.GetMapMeta(svc))
	mapGroup.GET("/:mapid/thumbnail", handlers.GetMapThumbnail(svc))
	mapGroup.DELETE("/:mapid", handlers.DeleteMap(svc))
	mapGroup.GET("/metadata", handlers.ListMeta(svc))
	mapGroup.POST("/:mapid/clear/:cleartime", handlers.AddMapClear(svc))
	mapGroup.POST("/:mapid/stat", handlers.IncreaseMapStat(svc))

	// Leaderboard APIs
	leaderboard := svc.Router.Group("/leaderboard")
	leaderboard.Use(authMiddleware(svc))

	leaderboard.POST("/user", handlers.PostPlayerRecord(svc))
	leaderboard.GET("/user/:username", handlers.GetPlayerRecord(svc))
	leaderboard.GET("/users", handlers.ListPlayerRecords(svc))
	leaderboard.DELETE("/user/:username", handlers.DeletePlayerRecord(svc))
}
