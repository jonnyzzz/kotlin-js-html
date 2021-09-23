package main

import (
	"bytes"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

func PublishS3PendingStatus(shaText string, status string, outputLines []string) {
	cacheBucketResultKey := GetCacheBucketResponsePath(shaText)

	payload := temporaryPayload(shaText, status, outputLines)

	_, err := s3Service.PutObject(&s3.PutObjectInput{
		Bucket: aws.String(GetCacheBucketName()),
		Key:    aws.String(cacheBucketResultKey),
		Body:   bytes.NewReader(payload),
	})

	if err != nil {
		log.Panicf("Failed to write results to S3 to %s: %v\n", cacheBucketResultKey, err)
	}
}

func PublishS3PendingStatusWithFiles(shaText string, status string, files []EcsDoneResultFile, outputLines []string) {
	cacheBucketResultKey := GetCacheBucketResponsePath(shaText)

	payload := finalPayload(shaText, status, files, outputLines)

	_, err := s3Service.PutObject(&s3.PutObjectInput{
		Bucket: aws.String(GetCacheBucketName()),
		Key:    aws.String(cacheBucketResultKey),
		Body:   bytes.NewReader(payload),
	})

	if err != nil {
		log.Panicf("Failed to write results to S3 to %s: %v\n", cacheBucketResultKey, err)
	}
}
