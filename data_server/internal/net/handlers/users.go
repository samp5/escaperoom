package handlers

import (
	"data_server/internal/dynamo"
	"data_server/internal/log"
	"data_server/internal/service"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

/*
:: POST /user/:username/:password

# Create a new user with the given username and password

REQUIRES the following PATH parameters:
  - "username": the username of the user to create
  - "password": the password of the user to log in with

RETURNS some json object, always containing "code" and "msg":

	On Success:
		- "code": 200
		- "msg": "OK"
		- "user": the created user
	On Fail:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func AddUser(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		// capture username and password from POST body
		user := dynamo.User{
			Name:     ctx.Param("username"),
			Password: ctx.Param("password"),
		}

		// ensure the user does not exist
		key, err := svc.Dynamo.GetUserKey(ctx, &user)
		if err != nil {
			log.WithError(err).Error("AddUser: could not check if user exists")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "could not check if user exists",
			})
			return
		} else if key != nil {
			ctx.JSON(http.StatusConflict, gin.H{
				"code": http.StatusConflict,
				"msg":  "Status Conflict",
				"err":  "user already exists",
			})
			return
		}

		// generate the enc_key, access_key, and user_id
		var err1, err2, err3 error
		user.ID, err1 = uuid.NewRandom()
		user.EncKey, err2 = uuid.NewRandom()
		user.AccessKey, err3 = uuid.NewRandom()
		if err1 != nil || err2 != nil || err3 != nil {
			log.Error("AddUser: could not generate UUIDs")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to generate UUIDs",
			})
			return
		}

		err = svc.Dynamo.AddUser(ctx, &user)
		if err != nil {
			log.WithError(err).Error("AddUser: could not add user to dynamo")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to add to dynamo",
			})
			return
		}

		user.Password = ""
		user.EncKey = uuid.UUID{}
		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg":  "OK",
			"user": user,
		})
	}
}

/*
:: HEAD /user/:username

# Check whether or not a user exists

REQUIRES the following PATH parameters:
  - "username": the username of the user to create

RETURNS only a code, and no object.

	If the user exists:
		- 200
	Otherwise:
		- non-200 identifying code
*/
func CheckUser(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		user := dynamo.User{
			Name: ctx.Param("username"),
		}
		key, err := svc.Dynamo.GetUserKey(ctx, &user)
		if err != nil {
			log.WithError(err).Error("CheckUser: could not get user key from dynamo")
			ctx.Status(http.StatusInternalServerError)
			return
		} else if key == nil {
			ctx.Status(http.StatusBadRequest)
			return
		}

		ctx.Status(http.StatusOK)
	}
}

/*
:: GET /user/:username/:password

# Attempt to get a user based on username and password combination

REQUIRES the following PATH parameters:
  - "username": the username of the user to create
  - "password": the password of the user to log in with

RETURNS some json object, always containing "code" and "msg":

	On username-password match:
		- "code": 200
		- "msg": "OK"
		- "user": the logged in user
	Otherwise:
		- "code": non-200
		- "msg": corresponding code message
		- "err": simplified error reason
*/
func Login(svc *service.Service) gin.HandlerFunc {
	return func(ctx *gin.Context) {
		user := dynamo.User{
			Name:     ctx.Param("username"),
			Password: ctx.Param("password"),
		}
		key, err := svc.Dynamo.GetUserKey(ctx, &user)
		if err != nil {
			log.WithError(err).Error("Login: could not get user key from dynamo")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to get user key",
			})
			return
		} else if key == nil {
			ctx.JSON(http.StatusBadRequest, gin.H{
				"code": http.StatusBadRequest,
				"msg":  "Bad Request",
				"err":  "user does not exist",
			})
			return
		}

		user.EncKey = *key
		qualUser, err := svc.Dynamo.GetUser(ctx, &user)
		if err != nil {
			log.WithError(err).Error("Login: could not get user from dynamo")
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"code": http.StatusInternalServerError,
				"msg":  "Internal Server Error",
				"err":  "failed to get user",
			})
			return
		} else if qualUser == nil {
			ctx.JSON(http.StatusForbidden, gin.H{
				"code": http.StatusForbidden,
				"msg":  "Forbidden",
				"err":  "username and password do not match",
			})
			return
		}

		qualUser.Password = ""
		qualUser.EncKey = uuid.UUID{}
		ctx.JSON(http.StatusOK, gin.H{
			"code": http.StatusOK,
			"msg":  "OK",
			"user": qualUser,
		})
	}
}
