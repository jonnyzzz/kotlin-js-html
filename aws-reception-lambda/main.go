package main

import (
	"context"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"github.com/aws/aws-lambda-go/events"
	runtime "github.com/aws/aws-lambda-go/lambda"
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
			fmt.Printf("Failed to decode body %s", err.Error())
			return events.APIGatewayProxyResponse{Body: "Failed to parse script", StatusCode: 400}, nil
		}
		kotlinCode = data
	} else {
		kotlinCode = []byte(request.Body)
	}

	sha := sha256.Sum256(kotlinCode)
	shaText := base64.URLEncoding.EncodeToString(sha[:])
	fmt.Printf("Kotlin Code hash = %s\n", shaText)

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
