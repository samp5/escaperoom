package handlers

import (
	"data_server/internal/log"
	"data_server/internal/service"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

/*
::POST /map

Post a map and its metadata.

This endpoint requires the following MUTLIPART information:
  - "upload": a zip file containing the map files
  - "meta": a json string of the metadata for the map
	- "thumbnail": a png image to use as the map thumbnail

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
		- "map_id": the published maps unique ID string
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func PostMap(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		meta := make(map[string]any)
		metastr := ctx.Request.FormValue("meta")
		if metastr == "" {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map upload must be accompanied with map metadata",
			})
			return
		}

		err := json.Unmarshal([]byte(metastr), &meta)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map upload metadata must be valid json",
			})
			return
		}

		file, _, err := ctx.Request.FormFile("upload")
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "File must be uploaded as a multipart named `upload`",
			})
			return
		}
		defer file.Close()

		thumb, _, err := ctx.Request.FormFile("thumbnail")
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "File must include a thumbnail multipart named `thumbnail`",
			})
			return
		}
		defer thumb.Close()

		mapID := uuid.New()
		meta["id"] = mapID.String()
		err = svc.Mongo.PutMetadata(ctx, meta)
		if err != nil {
			log.WithError(err).Error("PostMap: failed to upload map metadata to mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to upload map meta to mongodb",
			})
			return
		}

		err = svc.S3.UploadToS3(ctx, file, mapID.String())
		if err != nil {
			log.WithError(err).Error("PostMap: failed to upload map file to s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to upload map to s3",
			})
			return
		}

		err = svc.S3.UploadToS3(ctx, thumb, fmt.Sprintf("%s_thumb", mapID.String()))
		if err != nil {
			log.WithError(err).Error("PostMap: failed to upload thumbnail file to s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to upload map to s3",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code":   http.StatusOK,
			"msg":    "OK",
			"map_id": mapID.String(),
		})
	}
}

/*
::GET /map/:mapid

# Get a maps zip archive

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the map

On Success, RETURNS an OCTET-STREAM zip archive.
On Fail, RETURNS some json object:
  - "code": non-200
  - "msg": corresponding code message
  - "err": simplified error reason
*/
func GetMap(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		mapID := ctx.Param("mapid")
		_, err := uuid.Parse(mapID)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map upload must contain a valid uuid representing the maps ID",
			})
			return
		}

		data, err := svc.S3.GetFromS3(ctx, mapID)
		if err != nil {
			log.WithError(err).Error("GetMap: failed to read map from s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "Failed to read object from s3",
			})
			return
		}

		ctx.Header("Content-Type", "application/octet-stream")
		ctx.Header("Content-Encoding", "zip")
		ctx.DataFromReader(http.StatusOK, *data.Length, *data.Type, *data.Data, nil)
	}
}

/*
::GET /map/:mapid/metadata

# Get a maps metadata json

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the map

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
		- "metadata": the maps metadata json object
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func GetMapMeta(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		mapID := ctx.Param("mapid")
		_, err := uuid.Parse(mapID)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map metadata request must contain a valid uuid representing the maps ID",
			})
			return
		}

		meta, err := svc.Mongo.GetMetadata(ctx, mapID)
		if err != nil {
			log.WithError(err).Error("GetMapMeta: failed to read map metadata from mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "Failed to read metadata from mongodb",
			})
			return
		}

		if meta == nil {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code":     http.StatusOK,
			"msg":      "OK",
			"metadata": meta,
		})
	}
}

/*
::GET /map/:mapid/thumbnail

# Get a maps metadata thumbnail

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the map

On Success, RETURNS an OCTET-STREAM png image.
On Fail, RETURNS some json object:
  - "code": non-200
  - "msg": corresponding code message
  - "err": simplified error reason
*/
func GetMapThumbnail(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		mapID := ctx.Param("mapid")
		_, err := uuid.Parse(mapID)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map upload must contain a valid uuid representing the maps ID",
			})
			return
		}

		data, err := svc.S3.GetFromS3(ctx, fmt.Sprintf("%s_thumb", mapID))
		if err != nil {
			log.WithError(err).Error("GetMapThumbnail: failed to read map thumbnail from s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "Failed to read object from s3",
			})
			return
		}

		ctx.Header("Content-Type", "image/png")
		ctx.Header("Content-Encoding", "png")
		ctx.DataFromReader(http.StatusOK, *data.Length, *data.Type, *data.Data, nil)
	}
}

/*
::DELETE /map/:mapid

# Delete a map and all associated data

REQUIRES the following PATH parameters:
  - "mapid": the string UUID of the map

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func DeleteMap(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		mapID := ctx.Param("mapid")
		_, err := uuid.Parse(mapID)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "Map deletion must contain a valid uuid representing the maps ID",
			})
		}

		deleted, err := svc.Mongo.DeleteMetadata(ctx, mapID)
		if err != nil {
			log.WithError(err).Error("DeleteMap: failed to delete map metadata from mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to delete map metadata from mongodb",
			})
			return
		}
		if !deleted {
			ctx.JSON(http.StatusNotFound, gin.H{
				"code": http.StatusNotFound,
				"msg":  "Not Found",
				"err":  "no metadata found for given mapid",
			})
			return
		}

		err = svc.S3.RemoveFromS3(ctx, mapID)
		if err != nil {
			log.WithError(err).Error("DeleteMap: failed to delete map from s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to delete map from s3",
			})
			return
		}

		err = svc.S3.RemoveFromS3(ctx, fmt.Sprintf("%s_thumb", mapID))
		if err != nil {
			log.WithError(err).Error("DeleteMap: failed to delete map thumbnail from s3")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to delete map from s3",
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
	VALID_DIRS = []string{
		"asc",
		"desc",
	}
	VALID_METHODS = []string{
		"lte",
		"lt",
		"eq",
		"ne",
		"gt",
		"gte",
	}
	VALID_APPROACHES = []string{
		"and",
		"or",
	}
)

/*
::GET /maps

Return a list of map metadata, based on the given query parameters.

The following options are all supplied via QUERY parameters:

Standard options:
  - "limit": the maximum entries returned. default and maximum of 16

Sort options. If "sort_dir" is provided, "sort_field" is required. If
"sort_field" is provided, "sort_dir" is optional:
  - "sort_field": some field in the metadata. nested fields are separated
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
		- "metadata": a list of metadata json objects
		- "count": the number of metadata objects returned
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func ListMeta(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		args := expectFiltersForMongo(ctx)
		if ctx.IsAborted() {
			return
		}

		meta, err := svc.Mongo.ListMetadata(ctx, args)
		if err != nil {
			log.WithError(err).Error("ListMeta: failed to list map metadata from mongodb")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "Failed to list map metadata from mongodb",
			})
			return
		}

		ctx.JSON(http.StatusOK, gin.H{
			"code":     http.StatusOK,
			"msg":      "OK",
			"metadata": meta,
			"count":    len(meta),
		})
	}
}
