package s3

import (
	"context"
	"data_server/internal/config"
	"data_server/internal/log"
	"fmt"
	"io"
	"mime/multipart"

	"github.com/aws/aws-sdk-go-v2/aws"
	awsconfig "github.com/aws/aws-sdk-go-v2/config"
	awss3 "github.com/aws/aws-sdk-go-v2/service/s3"
)

type S3 struct {
	client *awss3.Client
	bucket string
}

func NewS3(cfg *config.Config) (*S3, error) {
	awscfg, err := awsconfig.LoadDefaultConfig(context.Background(), awsconfig.WithRegion("us-east-2"))
	if err != nil {
		log.WithError(err).Error("failed to load aws config")
		return nil, err
	}

	return &S3{
		client: awss3.NewFromConfig(awscfg),
		bucket: cfg.S3Bucket,
	}, nil
}

func (s3 *S3) UploadToS3(ctx context.Context, file multipart.File, mapID string) error {
	_, err := s3.client.PutObject(ctx, &awss3.PutObjectInput{
		Bucket: &s3.bucket,
		Key:    aws.String(fmt.Sprintf("maps/%s", mapID)),
		Body:   file,
	})

	if err != nil {
		log.WithError(err).Error("failed to upload map to s3")
		return err
	}

	return nil
}

type S3Object struct {
	Data   *io.ReadCloser
	Length *int64
	Type   *string
}

func (s3 *S3) GetFromS3(ctx context.Context, mapID string) (*S3Object, error) {
	obj, err := s3.client.GetObject(ctx, &awss3.GetObjectInput{
		Bucket: &s3.bucket,
		Key:    aws.String(fmt.Sprintf("maps/%s", mapID)),
	})
	if err != nil {
		log.WithError(err).Error("failed to retrieve map from s3")
		return nil, err
	}

	return &S3Object{
		Data:   &obj.Body,
		Length: obj.ContentLength,
		Type:   obj.ContentType,
	}, nil
}

func (s3 *S3) RemoveFromS3(ctx context.Context, mapID string) error {
	_, err := s3.client.DeleteObject(ctx, &awss3.DeleteObjectInput{
		Bucket: &s3.bucket,
		Key:    aws.String(fmt.Sprintf("maps/%s", mapID)),
	})
	if err != nil {
		log.WithError(err).Error("failed to delete map from s3")
		return err
	}

	return nil
}
