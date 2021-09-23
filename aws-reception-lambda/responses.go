package main

import "github.com/aws/aws-lambda-go/events"

func ApiGatewayResponseJson(data []byte) (events.APIGatewayProxyResponse, error) {
	return events.APIGatewayProxyResponse{
		Body: string(data),
		Headers: map[string]string{
			"Access-Control-Allow-Origin": "*",
			"Cache-Control":               "no-cache, max-age=0",
			"Content-Type":                "application/json; charset=UTF-8",
		},
		StatusCode: 200,
	}, nil
}

func temporaryResponse(shaText string, reason string) (events.APIGatewayProxyResponse, error) {
	data := temporaryPayload(shaText, reason)
	return ApiGatewayResponseJson(data)
}
