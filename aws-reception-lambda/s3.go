package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ecs"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
)

var s3Service *s3.S3
var ecsClient *ecs.ECS

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))

	s3Service = s3.New(sess)
	ecsClient = ecs.New(sess)

	TestS3()
}

func TestS3() {
	bucketName := GetCacheBucketName()
	log.Println("Processing bucket: ", bucketName)

	///this is warmup test for the lambda
	{
		objects, err := s3Service.ListObjects(&s3.ListObjectsInput{
			Bucket: aws.String(bucketName),
		})

		if err != nil {
			log.Panic("Failed to read bucket ", err.Error(), err)
		}

		for _, v := range objects.Contents {
			fmt.Println("Object: ", *v.Key)
		}
	}
}
