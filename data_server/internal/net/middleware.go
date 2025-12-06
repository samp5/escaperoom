package net

import (
	"data_server/internal/log"
	"data_server/internal/service"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

/*
Enforces the population of the following headers:
 - "auth_key"
*/
func authMiddleware(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		auth := ctx.GetHeader("Authorization")
		if auth == "" {
			log.WithField("auth", auth)
			ctx.AbortWithStatusJSON(http.StatusForbidden, gin.H{
				"code": http.StatusForbidden,
				"msg": "Forbidden",
			})
			return
		}

		components := strings.Split(auth, "@")
		if len(components) != 2 {
			ctx.AbortWithStatusJSON(http.StatusForbidden, gin.H{
				"code": http.StatusForbidden,
				"msg": "Forbidden",
			})
			return
		}

		logged, err := svc.Dynamo.VerifyUserKey(ctx, components[1], components[0])
		if err != nil {
			log.WithError(err).Error("authMiddleware: failed to verify user key")
			ctx.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg": "Internal Server Error",
			})
			return
		}
		if !logged {
			ctx.AbortWithStatusJSON(http.StatusForbidden, gin.H{
				"code": http.StatusForbidden,
				"msg": "Forbidden",
			})
			return
		}

		ctx.Keys = make(map[string]any)
		ctx.Keys["username"] = components[0]
		ctx.Keys["access_key"] = components[1]
		// log.WithField("api", ctx.FullPath()).Debug()
	}
}
