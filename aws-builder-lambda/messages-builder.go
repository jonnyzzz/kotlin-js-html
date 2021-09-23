package main

import (
	"bytes"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

func PublishS3PendingStatus(shaText string, status string) {
	PublishS3PendingStatusWithFiles(shaText, status, []EcsDoneResultFile{})
}

func PublishS3PendingStatusWithFiles(shaText string, status string, files []EcsDoneResultFile) {
	cacheBucketResultKey := GetCacheBucketResponsePath(shaText)

	payload := finalPayload(shaText, status, files, "Not Implemented")

	_, err := s3Service.PutObject(&s3.PutObjectInput{
		Bucket: aws.String(GetCacheBucketName()),
		Key:    aws.String(cacheBucketResultKey),
		Body:   bytes.NewReader(payload),
	})

	if err != nil {
		log.Panicf("Failed to write results to S3 to %s: %v\n", cacheBucketResultKey, err)
	}
}
