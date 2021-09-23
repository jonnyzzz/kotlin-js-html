package main

import (
	"context"
	"fmt"
	"github.com/aws/aws-lambda-go/events"
	runtime "github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"io/ioutil"
)

func handleRequest(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Processing request data for request %s.\n", request.RequestContext.RequestID)
	fmt.Printf("Body size = %d.\n", len(request.Body))
	fmt.Println("Headers:")

	for key, value := range request.Headers {
		fmt.Printf("    %s: %s\n", key, value)
	}

	cacheBucketName := GetCacheBucketName()
	cacheBucketResultKey := request.PathParameters["suffix"]

	fmt.Printf("Checking S3 for result at %s %s\n", cacheBucketName, cacheBucketResultKey)
	resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(cacheBucketResultKey),
	})

	if resultObject != nil && err == nil {
		fmt.Printf("The result is cached in S3, returning as-is from%s\n", cacheBucketResultKey)
		payload, err := ioutil.ReadAll(resultObject.Body)
		if err == nil {
			return resultResponse(payload)
		}
	}

	return errorResponse()
}

func main() {
	runtime.Start(handleRequest)
}
