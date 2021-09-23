package main

import (
	"encoding/json"
	"github.com/aws/aws-lambda-go/events"
	"log"
)

type EcsPendingResult struct {
	Type   string `json:"type"`
	Status string `json:"status"`
	TaskId string `json:"task_id"`
}

func GeneratePendingMessage(status string, taskId string) []byte {
	data, err := json.MarshalIndent(EcsPendingResult{
		Type:   "pending",
		Status: status,
		TaskId: taskId,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}

	return data
}

type RetryAfterTimeoutMessage struct {
	Type          string `json:"type"`
	TimeoutMillis int    `json:"timeout_millis"`
	Sha256        string `json:"sha_256"`
	Reason        string `json:"reason"`
}

type ResultMessage struct {
	Type    string      `json:"type"`
	Results interface{} `json:"results"`
	Sha256  string      `json:"sha_256"`
}

func temporaryResponse(shaText string, reason string) (events.APIGatewayProxyResponse, error) {
	data, err := json.MarshalIndent(RetryAfterTimeoutMessage{
		Type:          "retry-after-timeout",
		TimeoutMillis: 777,
		Sha256:        shaText,
		Reason:        reason,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}

	return events.APIGatewayProxyResponse{
		Body: string(data),
		Headers: map[string]string{
			"Access-Control-Allow-Origin": "*",
			"Cache-Control":               "no-cache, max-age=0",
			"Content-Type":                "application/json; charset=UTF-8",
			//"ETag":          fmt.Sprintf("temp-%s", uuid.NewString()),
		},
		StatusCode: 200,
	}, nil
}

func resultResponse(shaText string, payload []byte) (events.APIGatewayProxyResponse, error) {
	var parsedPayload interface{}
	err := json.Unmarshal(payload, &parsedPayload)
	if err != nil {
		log.Panic("Failed to parse result payload from S3", err.Error(), err)
	}

	message, err := json.MarshalIndent(ResultMessage{
		Type:    "result",
		Results: parsedPayload,
		Sha256:  shaText,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}

	//TODO: allow caching once debug phase is completed
	return events.APIGatewayProxyResponse{
		Body: string(message),
		Headers: map[string]string{
			"Cache-Control":               "no-cache, max-age=0",
			"Content-Type":                "application/json; charset=UTF-8",
			"Access-Control-Allow-Origin": "*",
			//"ETag":          fmt.Sprintf("result-%s", shaText),
		},
		StatusCode: 200,
	}, nil
}
