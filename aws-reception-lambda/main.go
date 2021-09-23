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
	"github.com/aws/aws-sdk-go/service/ecs"
	"github.com/aws/aws-sdk-go/service/s3"
)

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
			fmt.Printf("Failed to decode body %s\n", err.Error())
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

	fmt.Printf("Checking S3 for result at %s %s\n", cacheBucketName, cacheBucketKeyName)
	resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(cacheBucketKeyName),
	})

	if resultObject != nil && err == nil {
		fmt.Printf("The result is cached in S3, returning as-is from%s\n", cacheBucketKeyName)
		return resultResponse(shaText, []byte(resultObject.String()))
	}

	if aer, ok := err.(awserr.Error); !ok || aer.Code() != s3.ErrCodeNoSuchKey {
		fmt.Printf("Failed to get cached object from S3. %s %v\n", err.Error(), err)
		return temporaryResponse(shaText, "Failed to read caches")
	}

	fmt.Printf("No object in the cache for %s\n", shaText)

	startedTask, err := ecsClient.RunTask(&ecs.RunTaskInput{
		Count:          aws.Int64(1),
		Cluster:        aws.String(GetEcsClusterName()),
		LaunchType:     aws.String("FARGATE"),
		TaskDefinition: aws.String(GetEcsTaskDefinitionArn()),
		NetworkConfiguration: &ecs.NetworkConfiguration{
			AwsvpcConfiguration: &ecs.AwsVpcConfiguration{
				AssignPublicIp: aws.String("ENABLED"),
				Subnets:        aws.StringSlice(GetEcsTaskSubnets()),
			},
		},
	})

	//startedTask, err := ecsClient.StartTask(&ecs.StartTaskInput{
	//	Cluster: aws.String(GetEcsClusterName()),
	//
	//  TaskDefinition: aws.String(GetEcsTaskDefinitionArn()),
	//  NetworkConfiguration: &ecs.NetworkConfiguration{
	//    AwsvpcConfiguration: &ecs.AwsVpcConfiguration{
	//      AssignPublicIp: aws.String("ENABLED"),
	//      Subnets: aws.StringSlice(GetEcsTaskSubnets()),
	//    },
	//  },
	//})

	if err != nil {
		fmt.Printf("Failed to start ECS task: %v\n", err)
	}

	fmt.Printf("Started ECS task: %v\n", startedTask.GoString())

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
