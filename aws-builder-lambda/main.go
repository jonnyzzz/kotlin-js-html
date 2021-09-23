package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"io/fs"
	"io/ioutil"
	"log"
	"os"
)

func main() {
	fmt.Println("Running Gradle build...")

	//cacheBucketResultKey := GetCacheBucketResponsePath(shaText)
	//cacheBucketInputKey := GetCacheBucketInputPath(shaText)

	cacheBucketName := GetCacheBucketName()
	shaText := GetShaRequest()
	sourceInputKey := GetTaskInput()

	fmt.Printf("Checking S3 for result at %s %s\n", cacheBucketName, sourceInputKey)
	resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(sourceInputKey),
	})

	if resultObject == nil || err != nil {
		log.Fatalf("Failed to find sources object from S3. %v\n", err)
	}

	fmt.Printf("The result is cached in S3, returning as-is from%s\n", sourceInputKey)
	kotlinCode, err := ioutil.ReadAll(resultObject.Body)
	if err != nil {
		log.Fatalf("Failed to get download sources object from S3. %v\n", err)
	}

	inputFilePath := "/runner/input-file"
	err = ioutil.WriteFile(inputFilePath, kotlinCode, fs.ModePerm)
	if err != nil {
		log.Fatalf("Failed to save downloaded sources on disk. %v\n", err)
	}

	_ = os.Setenv("INPUT_FILE", inputFilePath)

	//sha := sha256.Sum256(kotlinCode)
	//shaText := base64.URLEncoding.EncodeToString(sha[:])
	fmt.Printf("Kotlin Code hash = %s\n", shaText)

	RunGradle()
}
