package main

import (
	"bytes"
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
	"io/ioutil"
	"strings"
)

func handleRequest(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Processing request data for request %s.\n", request.RequestContext.RequestID)
	fmt.Printf("Body size = %d.\n", len(request.Body))
	fmt.Println("Headers:")

	forceRebuild := false

	for key, value := range request.Headers {
		fmt.Printf("    %s: %s\n", key, value)

		if strings.ToLower(key) == strings.ToLower("X-KT-JS-REBUILD") {
			forceRebuild = true
		}
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

	if !forceRebuild {
		fmt.Printf("Checking S3 for result at %s %s\n", cacheBucketName, cacheBucketKeyName)
		resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
			Bucket: aws.String(cacheBucketName),
			Key:    aws.String(cacheBucketKeyName),
		})

		if resultObject != nil && err == nil {
			fmt.Printf("The result is cached in S3, returning as-is from%s\n", cacheBucketKeyName)
			payload, err := ioutil.ReadAll(resultObject.Body)
			if err == nil {
				return resultResponse(shaText, payload)
			}
		}

		if aer, ok := err.(awserr.Error); !ok || aer.Code() != s3.ErrCodeNoSuchKey {
			fmt.Printf("Failed to get cached object from S3. %s %v\n", err.Error(), err)
			return temporaryResponse(shaText, "Failed to read caches")
		}
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
		Overrides: &ecs.TaskOverride{
			ContainerOverrides: []*ecs.ContainerOverride{
				{
					Name: aws.String("builder"),
					Environment: []*ecs.KeyValuePair{
						{
							Name:  aws.String("New Variable"),
							Value: aws.String("Test"),
						},
					},
				},
			},
		},
	})

	if err != nil {
		fmt.Printf("Failed to start ECS task: %v\n", err)
		return temporaryResponse(shaText, "Failed to start builder")
	}

	_, err = s3Service.PutObject(&s3.PutObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(cacheBucketKeyName),
		Body:   bytes.NewReader(GeneratePendingMessage("Builder has started", *startedTask.Tasks[0].TaskArn)),
	})

	if err != nil {
		fmt.Printf("Failed to write status to S3: %v\n", err)
		return temporaryResponse(shaText, "Failed write builder status")
	}

	fmt.Printf("Started ECS task: %v\n", startedTask.GoString())
	return temporaryResponse(shaText, "Building, please come back later")
}

func main() {
	runtime.Start(handleRequest)
}
