package main

import (
	"bytes"
	"encoding/json"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

type EcsDoneResult struct {
	Type   string `json:"type"`
	Status string `json:"status"`
}

func PublishS3PendingStatus(shaText string, status string) {
	cacheBucketResultKey := GetCacheBucketResponsePath(shaText)

	payload, err := json.MarshalIndent(EcsDoneResult{
		Type:   "builder-task",
		Status: status,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}

	_, err = s3Service.PutObject(&s3.PutObjectInput{
		Bucket: aws.String(GetCacheBucketName()),
		Key:    aws.String(cacheBucketResultKey),
		Body:   bytes.NewReader(payload),
	})
}
