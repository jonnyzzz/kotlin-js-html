package main

import (
	"context"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"github.com/aws/aws-lambda-go/events"
	runtime "github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/awserr"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

var s3Service *s3.S3

func init() {
	awsSession, err := session.NewSession()
	if err != nil {
		log.Panic("Failed to open AWS session", err.Error(), err)
		return
	}

	s3Service = s3.New(awsSession)
}

func handleRequest(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Processing request data for request %s.\n", request.RequestContext.RequestID)
	fmt.Printf("Body size = %d.\n", len(request.Body))
	fmt.Println("Headers:")
	for key, value := range request.Headers {
		fmt.Printf("    %s: %s\n", key, value)
	}

	var kotlinCode []byte
	if request.IsBase64Encoded {
		data, err := base64.StdEncoding.DecodeString(request.Body)
		if err != nil {
			fmt.Printf("Failed to decode body %s", err.Error())
			return events.APIGatewayProxyResponse{Body: "Failed to parse script", StatusCode: 400}, nil
		}
		kotlinCode = data
	} else {
		kotlinCode = []byte(request.Body)
	}

	//TODO: include compilation mode to the SHA
	sha := sha256.Sum256(kotlinCode)
	shaText := base64.URLEncoding.EncodeToString(sha[:])
	fmt.Printf("Kotlin Code hash = %s\n", shaText)

	cacheBucketKeyName := GetCacheBucketResponsePath(shaText)
	cacheBucketName := GetCacheBucketName()

	fmt.Printf("Checking S3 for result at %s %s", cacheBucketName, cacheBucketKeyName)
	resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(cacheBucketKeyName),
	})

	if resultObject != nil && err == nil {
		fmt.Printf("The result is cached in S3, returning as-is from%s\n", cacheBucketKeyName)
		return resultResponse(shaText, []byte(resultObject.String()))
	}

	if aer, ok := err.(awserr.Error); !ok || aer.Code() != s3.ErrCodeNoSuchKey {
		msg := fmt.Sprint("Failed to get cached data", err.Error(), err)
		fmt.Print(msg)
		return temporaryResponse(shaText, "Failed to read caches")
	}

	fmt.Printf("No object in the cache for %s\n", shaText)
	//TODO: start new lambda and return the result
	return mockResponse(shaText, kotlinCode)
}

func mockResponse(shaText string, kotlinCode []byte) (events.APIGatewayProxyResponse, error) {
	return events.APIGatewayProxyResponse{
		Body: fmt.Sprintf("this is our lambda:%s\n%s\n", shaText, string(kotlinCode)),
		Headers: map[string]string{
			"ETag":         shaText,
			"Content-Type": "application/json",
		},
		StatusCode: 200,
	}, nil
}

func main() {
	runtime.Start(handleRequest)
}
