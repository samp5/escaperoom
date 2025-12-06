package env

import (
	"context"
	"data_server/internal/service"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	ginadapter "github.com/awslabs/aws-lambda-go-api-proxy/gin"
)

func RunLambda(svc *service.Service) {
	lambda.Start(
		func(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
			ginLambda := ginadapter.New(svc.Router)
			return ginLambda.ProxyWithContext(ctx, request)
		},
	)
}
