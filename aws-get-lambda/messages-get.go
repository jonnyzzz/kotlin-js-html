package main

import (
	"github.com/aws/aws-lambda-go/events"
)

func resultResponse(payload []byte) (events.APIGatewayProxyResponse, error) {
	//TODO: allow caching once debug phase is completed
	return events.APIGatewayProxyResponse{
		Body: string(payload),
		Headers: map[string]string{
			"Cache-Control":               "no-cache, max-age=0",
			"Content-Type":                "application/json; charset=UTF-8",
			"Access-Control-Allow-Origin": "*",
		},
		StatusCode: 200,
	}, nil
}

func errorResponse() (events.APIGatewayProxyResponse, error) {
	//TODO: allow caching once debug phase is completed
	return events.APIGatewayProxyResponse{
		Headers: map[string]string{
			"Cache-Control":               "no-cache, max-age=0",
			"Content-Type":                "application/json; charset=UTF-8",
			"Access-Control-Allow-Origin": "*",
		},
		StatusCode: 404,
	}, nil
}
