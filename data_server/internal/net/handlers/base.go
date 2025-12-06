package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func NotFoundHandler(ctx *gin.Context) {
	ctx.JSON(http.StatusNotFound, gin.H{
		"code": http.StatusNotFound,
		"msg":  "Not Found",
	})
}
