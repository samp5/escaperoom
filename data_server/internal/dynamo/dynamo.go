package dynamo

import (
	"context"
	"data_server/internal/encryption"
	"data_server/internal/log"
	"fmt"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/feature/dynamodb/attributevalue"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb/types"
	"github.com/google/uuid"
)

var (
	MAP_TABLE  = aws.String("MapData")
	USER_TABLE = aws.String("Users")
)

type Dynamo struct {
	client *dynamodb.Client
}

func NewDynamo() (*Dynamo, error) {
	awscfg, err := config.LoadDefaultConfig(context.Background(), config.WithRegion("us-east-2"))
	if err != nil {
		log.WithError(err).Error("failed to load aws config")
		return nil, err
	}

	return &Dynamo{
		client: dynamodb.NewFromConfig(awscfg),
	}, nil
}

func (dyn *Dynamo) AddUser(ctx context.Context, input *User) error {
	userKeyItem, err := attributevalue.MarshalMap(map[string]any{
		"pk":  fmt.Sprintf("USERNAME#%s", input.Name),
		"sk":  "USERKEY",
		"key": input.EncKey,
	})
	if err != nil {
		log.WithError(err).Error("AddUser: error creating userKeyItem")
		return err
	}

	encoder := encryption.NewEncryptor(input.EncKey)
	encPw := encoder.EncryptPassword(input.Password)
	userItem, err := attributevalue.MarshalMap(map[string]any{
		"pk":  fmt.Sprintf("USERNAME#%s", input.Name),
		"sk":  fmt.Sprintf("PASSWORD#%s", encPw),
		"key": input.AccessKey,
		"id":  input.ID,
	})
	if err != nil {
		log.WithError(err).Error("AddUser: error creating userItem")
		return err
	}

	keyUserItem, err := attributevalue.MarshalMap(map[string]any{
		"pk": fmt.Sprintf("USERNAME#%s", input.Name),
		"sk": fmt.Sprintf("KEY#%s", input.AccessKey),
	})
	if err != nil {
		log.WithError(err).Error("AddUser: error creating userItem")
		return err
	}
	_, err = dyn.client.BatchWriteItem(ctx, &dynamodb.BatchWriteItemInput{
		RequestItems: map[string][]types.WriteRequest{
			*USER_TABLE: {
				types.WriteRequest{
					PutRequest: &types.PutRequest{
						Item: userKeyItem,
					},
				},
				types.WriteRequest{
					PutRequest: &types.PutRequest{
						Item: userItem,
					},
				},
				types.WriteRequest{
					PutRequest: &types.PutRequest{
						Item: keyUserItem,
					},
				},
			},
		},
	})

	if err != nil {
		log.WithError(err).Error("AddUser: failed to upload to dynamo")
		return err
	}

	return nil
}

func (dyn *Dynamo) GetUserKey(ctx context.Context, input *User) (*uuid.UUID, error) {
	key, err := attributevalue.MarshalMap(map[string]any{
		"pk": fmt.Sprintf("USERNAME#%s", input.Name),
		"sk": "USERKEY",
	})
	if err != nil {
		log.WithError(err).Error("GetUserKey: error creating item key")
		return nil, err
	}

	out, err := dyn.client.GetItem(ctx, &dynamodb.GetItemInput{
		Key:       key,
		TableName: USER_TABLE,
	})
	if err != nil {
		log.WithError(err).Error("GetUserKey: error getting user key from dynamo")
		return nil, err
	}

	if out.Item == nil {
		return nil, nil
	}

	var userKey uuid.UUID
	err = attributevalue.Unmarshal(out.Item["key"], &userKey)
	if err != nil {
		log.WithError(err).Error("GetUserKey: error unmarshalling user key from dynamo")
		return nil, err
	}

	return &userKey, nil
}

func (dyn *Dynamo) GetUser(ctx context.Context, input *User) (*User, error) {
	enc := encryption.NewEncryptor(input.EncKey)
	key, err := attributevalue.MarshalMap(map[string]any{
		"pk": fmt.Sprintf("USERNAME#%s", input.Name),
		"sk": fmt.Sprintf("PASSWORD#%s", enc.EncryptPassword(input.Password)),
	})
	if err != nil {
		log.WithError(err).Error("GetUser: error creating item key")
		return nil, err
	}

	out, err := dyn.client.GetItem(ctx, &dynamodb.GetItemInput{
		Key:       key,
		TableName: USER_TABLE,
	})
	if err != nil {
		log.WithError(err).Error("GetUser: error getting user key from dynamo")
		return nil, err
	}

	if out.Item == nil {
		return nil, nil
	}

	ret := User{}
	err = attributevalue.Unmarshal(out.Item["key"], &ret.AccessKey)
	if err != nil {
		log.WithError(err).Error("GetUser: error unmarshalling access key from dynamo")
		return nil, err
	}
	err = attributevalue.Unmarshal(out.Item["id"], &ret.ID)
	if err != nil {
		log.WithError(err).Error("GetUser: error unmarshalling id from dynamo")
		return nil, err
	}

	return &ret, nil
}

func (dyn *Dynamo) VerifyUserKey(ctx context.Context, userKey string, username string) (bool, error) {
	key, err := attributevalue.MarshalMap(map[string]any{
		"pk": fmt.Sprintf("USERNAME#%s", username),
		"sk": fmt.Sprintf("KEY#%s", userKey),
	})
	if err != nil {
		log.WithError(err).Error("VerifyUserKey: error creating item key")
		return false, err
	}

	verify, err := dyn.client.GetItem(ctx, &dynamodb.GetItemInput{
		Key:       key,
		TableName: USER_TABLE,
	})
	if err != nil {
		log.WithError(err).Error("VerifyUserKey: error verifying user key")
		return false, err
	}

	if verify.Item == nil {
		return false, nil
	}

	return true, nil
}
