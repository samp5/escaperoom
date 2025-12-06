package handlers

import (
	"data_server/internal/log"
	"data_server/internal/service"
	"net/http"

	"github.com/gin-gonic/gin"
)

/*
::POST /leaderboard/user

Post and update the user's player records to the leaderboard

This endpoint requires the following BODY:
	- some json object representing the player record

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func PostPlayerRecord(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		record := make(map[string]any)
		err := ctx.ShouldBindBodyWithJSON(&record)
		if (err != nil) {
			log.WithError(err).Error("PostLeaderboard: unable to map body to json")
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg": "Bad Request",
				"err": "body must be a json player record",
			})
			return
		}

		err = svc.Mongo.PutPlayerRecord(ctx, record)
		if err != nil {
			log.WithError(err).Error("PostLeaderboard: unable to upload record to mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg": "Internal Server Error",
				"err": "failed to upload to mongodb",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg": "OK",
		})
	}
}

/*
::GET /leaderboard/user/:username

Get the user's player records

REQUIRES the following PATH parameters:
  - "username": the username of the leaderboard entries player

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
		- "record": the record in json
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func GetPlayerRecord(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		record, err := svc.Mongo.GetPlayerRecord(ctx, ctx.Param("username"))
		if err != nil {
			log.WithError(err).Error("GetPlayerRecord: unable to fetch player record")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg": "Internal Server Error",
				"err": "failed to fetch record from mongodb",
			})
			return
		}

		if record == nil {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
				"err":  "No such record",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code":   http.StatusOK,
			"msg":    "OK",
			"record": record,
		})
	}
}

/*
::GET /leaderboard/users

List user leaderboards, based on given filters and sort

The following options are all supplied via QUERY parameters:

Standard options:
  - "limit": the maximum entries returned. default and maximum of 16

Sort options. If "sort_dir" is provided, "sort_field" is required. If
"sort_field" is provided, "sort_dir" is optional:
  - "sort_field": some field in the record. nested fields are separated
    via a "."; ex: "stats.clears". An invalid field is ignored
  - "sort_dir": one of "asc" or "desc". default of "desc"

Filtering options. Multiple copies of each are allowed, which creates multiple
applying filters. Each listed field must appear an equal amount of times. The
"N"th appearance of each key is used in conjunction. "filters_approach" is
optional in the case of multiple filters, but an error if 0 or 1 filters
applied:
  - "filter_field": the field to filter on
  - "filter_method": one of "lte", "lt", "eq", "neq", "gt", "gte"
  - "filter_value": some value to match on
  - "filter_type": one of "int" or "str"
  - "filters_approach": not a list. one of "and" or "or"

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
		- "records": a list of user record json objects
		- "count": the number of record objects returned
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func ListPlayerRecords(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		args := expectFiltersForMongo(ctx)
		if ctx.IsAborted() {
			return
		}

		records, err := svc.Mongo.ListPlayerRecords(ctx, args)
		if err != nil {
			log.WithError(err).Error("ListPlayerRecords: failed to list player records from mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "Failed to list player records from mongodb",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code":     http.StatusOK,
			"msg":      "OK",
			"records": records,
			"count":    len(records),
		})
	}
}

/*
::DELETE /user/:username

# Delete a user's leaderboard record

REQUIRES the following PATH parameters:
  - "username": the username of the leaderboard entries player

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func DeletePlayerRecord(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		deleted, err := svc.Mongo.DeletePlayerRecord(ctx, ctx.Param("username"))
		if err != nil {
			log.WithError(err).Error("DeletePlayerRecord: failed to delete player record from mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to delete player record from mongodb",
			})
			return
		}
		if !deleted {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
				"err":  "no record found for given username",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg":  "OK",
		})
	}
}
