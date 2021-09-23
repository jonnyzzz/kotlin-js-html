package main

import (
	"bytes"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"io/fs"
	"io/ioutil"
	"log"
	"os"
	"path"
	"path/filepath"
)

func main() {
	fmt.Println("Running Gradle build...")

	cacheBucketName := GetCacheBucketName()
	shaText := GetShaRequest()
	sourceInputKey := GetTaskInput()

	fmt.Printf("Checking S3 for result at %s %s\n", cacheBucketName, sourceInputKey)
	resultObject, err := s3Service.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(cacheBucketName),
		Key:    aws.String(sourceInputKey),
	})

	if resultObject == nil || err != nil {
		log.Panicf("Failed to find sources object from S3. %v\n", err)
	}

	fmt.Printf("The result is cached in S3, returning as-is from%s\n", sourceInputKey)
	kotlinCode, err := ioutil.ReadAll(resultObject.Body)
	if err != nil {
		log.Panicf("Failed to get download sources object from S3. %v\n", err)
	}

	inputFilePath := "/runner/input-file"
	err = ioutil.WriteFile(inputFilePath, kotlinCode, fs.ModePerm)
	if err != nil {
		log.Panicf("Failed to save downloaded sources on disk. %v\n", err)
	}

	outputDirPath := "/runner/output-dir"
	_ = os.RemoveAll(outputDirPath)
	_ = os.MkdirAll(outputDirPath, os.ModePerm)
	_ = os.Setenv("INPUT_FILE", inputFilePath)
	_ = os.Setenv("OUTPUT_DIR", outputDirPath)

	//sha := sha256.Sum256(kotlinCode)
	//shaText := base64.URLEncoding.EncodeToString(sha[:])
	fmt.Printf("Kotlin Code hash = %s\n", shaText)

	PublishS3PendingStatus(shaText, "starting")

	//TODO: include output logs to S3 file
	err = RunGradle()

	if err != nil {
		PublishS3PendingStatus(shaText, "failed")
		return
	}

	var files []string
	err = filepath.Walk(outputDirPath, func(path string, info os.FileInfo, err error) error {
		info, err = os.Stat(path)
		if err == nil && info.Mode().IsRegular() {
			files = append(files, path)
		}
		return nil
	})

	if err != nil {
		log.Panicf("Failed to list files under %s. %v\n", outputDirPath, err)
	}

	for _, file := range files {
		fmt.Println("Generated file: ", file)
	}

	for _, file := range files {
		fmt.Println("Uploading file: ", file)
		_, filename := path.Split(file)

		cacheBucketResultKey := GetCacheBucketResultFilePath(shaText, filename)
		payload, err := ioutil.ReadFile(file)
		if err != nil {
			log.Panicf("Failed to read file from %s. %v\n", filename, err)
		}

		_, err = s3Service.PutObject(&s3.PutObjectInput{
			Bucket: aws.String(cacheBucketName),
			Key:    aws.String(cacheBucketResultKey),
			Body:   bytes.NewReader(payload),
		})

		if err != nil {
			log.Panicf("Failed to write results to S3 to %s: %v\n", cacheBucketResultKey, err)
		}
	}

	PublishS3PendingStatus(shaText, "success")
}
