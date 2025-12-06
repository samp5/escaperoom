package handlers

import (
	"data_server/internal/log"
	"data_server/internal/mongodb"
	"fmt"
	"net/http"
	"slices"
	"strconv"

	"github.com/gin-gonic/gin"
)

func expectFiltersForMongo(ctx *gin.Context) *mongodb.ListArguments {
	args := mongodb.ListArguments{}

	limitStr := ctx.Query("limit")
	args.Limit = 16
	if limitStr != "" {
		var err error
		args.Limit, err = strconv.ParseInt(limitStr, 10, 64)
		if err != nil || args.Limit < 1 || args.Limit > 16 {
			ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "option `limit` must be a number between and including 1 and 16",
			})
			return nil
		}
	}

	args.SortField = ctx.Query("sort_field")

	sortDirStr := ctx.Query("sort_dir")
	args.SortDir = -1
	if sortDirStr != "" {
		if !slices.Contains(VALID_DIRS, sortDirStr) {
			ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "option `sort_dir` must be one of `asc` or `desc`",
			})
			return nil
		} else if args.SortField == "" {
			ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "when using `sort_dir`, `sort_field` is required",
			})
			return nil
		}

		if sortDirStr == "asc" {
			args.SortDir = 1
		}
	}

	args.FilterFields = ctx.QueryArray("filter_field")
	args.FilterMethods = ctx.QueryArray("filter_method")
	filterValues := ctx.QueryArray("filter_value")
	filterTypes := ctx.QueryArray("filter_type")
	args.FiltersApproach = ctx.Query("filters_approach")

	if !(len(args.FilterFields) == len(args.FilterMethods) && len(args.FilterFields) == len(filterValues) && len(args.FilterFields) == len(filterTypes)) {
		ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
			"code": http.StatusBadRequest,
			"msg":  "Bad Request",
			"err":  "`filter_field`, `filter_method`, `filter_value`, and `filter_type` must be the same length",
		})
		return nil
	}
	if len(args.FilterFields) <= 1 && args.FiltersApproach != "" {
		ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
			"code": http.StatusBadRequest,
			"msg":  "Bad Request",
			"err":  "`filters_approach` should not be provided if one or no filters are being applied",
		})
		return nil
	}
	for _, method := range args.FilterMethods {
		if !slices.Contains(VALID_METHODS, method) {
			log.Info(method)
			ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "`filter_method` must be one of `lte`, `lt`, `eq`, `neq`, `gt`, `gte`",
			})
			return nil
		}
	}
	if args.FiltersApproach != "" && !slices.Contains(VALID_APPROACHES, args.FiltersApproach) {
		ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
			"code": http.StatusBadRequest,
			"msg":  "Bad Request",
			"err":  "`filters_approach` must be one of `and` or `or`",
		})
		return nil
	}
	if args.FiltersApproach == "" {
		args.FiltersApproach = "or"
	}
	args.FilterValues = make([]any, 0, len(filterValues))
	for index := range filterValues {
		switch filterTypes[index] {
		case "str":
			args.FilterValues = append(args.FilterValues, filterValues[index])

		case "int":
			value, err := strconv.ParseInt(filterValues[index], 10, 32)
			if err != nil {
				ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
					"code": http.StatusBadRequest,
					"msg":  "Bad Request",
					"err":  fmt.Sprintf("filter value `%s` could not be interpreted as an int", args.FilterValues[index]),
				})
				return nil
			}
			args.FilterValues = append(args.FilterValues, value)

		default:
			ctx.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "`filter_type` must be one of `int` or `str`",
			})
			return nil
		}
	}

	return &args
}
