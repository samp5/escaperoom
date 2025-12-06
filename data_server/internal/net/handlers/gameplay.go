package handlers

import (
	"data_server/internal/log"
	"data_server/internal/service"
	"net/http"
	"slices"
	"strconv"

	"github.com/gin-gonic/gin"
)

/*
:: POST /map/:mapid/clear/:cleartime

Add a map clear to the stored metadata, and update the fastest clear if needed.

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the cleared map
  - "cleartime": the cleartime in milliseconds of this clear

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func AddMapClear(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		mapID := ctx.Param("mapid")
		if mapID == "" {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "mapid must be given",
			})
			return
		}
		cleartimeStr := ctx.Param("cleartime")
		if cleartimeStr == "" {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "cleartime must be given",
			})
			return
		}
		cleartime, err := strconv.ParseInt(cleartimeStr, 10, 64)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "cleartime must be a number in milliseconds",
			})
			return
		}

		found, err := svc.Mongo.UpdateMapForClear(ctx, ctx.Keys["username"].(string), mapID, cleartime)
		if err != nil {
			log.WithError(err).Error("AddMapClear: failed to update map attempt count")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to update map attempt count",
			})
			return
		} else if found != nil {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
				"err":  "Given map does not exist",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg":  "OK",
		})
	}
}

var (
	VALID_STAT_INCREASES = []string{
		"downloads",
		"attempts",
		"upvotes",
		"downvotes",
	}
)

/*
:: POST /map/:mapid/stat

# Increase map metadata stat numbers

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the cleared map

OPTIONALLY allows the following QUERY parameters:
  - "downloads": presence. increase download count by 1
  - "attempts": presence. increase attempt count by 1
  - "upvotes": presence. increase upvote count by 1
  - "downvotes": presence. increase downvote count by 1

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func IncreaseMapStat(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		id := ctx.Param("mapid")
		if id == "" {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "mapid must be given",
			})
			return
		}

		// make a set of found valid query parameters. set to exclude duplicate keys
		increases := make(map[string]struct{}, len(VALID_STAT_INCREASES))
		for name := range ctx.Request.URL.Query() {
			if slices.Contains(VALID_STAT_INCREASES, name) {
				increases[name] = struct{}{}
			}
		}

		found, err := svc.Mongo.UpdateMapStats(ctx, id, increases)
		if err != nil {
			log.WithError(err).Error("IncreaseMapStat: failed to update map stat count")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to update map stat count",
			})
			return
		} else if found != nil {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
				"err":  "Given map does not exist",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg":  "OK",
		})
	}
}
