package main

import (
	"bytes"
	"encoding/json"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

type EcsDoneResultFile struct {
	BucketKey string `json:"key"`
	CdnUrl    string `json:"cnd_url"`
}

type EcsDoneResult struct {
	Type   string              `json:"type"`
	Status string              `json:"status"`
	Files  []EcsDoneResultFile `json:"files"`
}

func PublishS3PendingStatus(shaText string, status string) {
	PublishS3PendingStatusWithFiles(shaText, status, []EcsDoneResultFile{})
}

func PublishS3PendingStatusWithFiles(shaText string, status string, files []EcsDoneResultFile) {
	cacheBucketResultKey := GetCacheBucketResponsePath(shaText)

	payload, err := json.MarshalIndent(EcsDoneResult{
		Type:   "builder-task",
		Status: status,
		Files:  files,
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
