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
	"sync"
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

	PublishS3PendingStatus(shaText, "starting", []string{})

	var wg sync.WaitGroup
	wg.Add(1)

	outputChannel := make(chan []string, 100)
	go func() {
		defer wg.Done()

		for value := range outputChannel {
			PublishS3PendingStatus(shaText, "running", value)
		}
	}()

	//TODO: include output logs to S3 file
	outputLines, err := RunGradle(outputChannel)

	wg.Wait()

	if err != nil {
		PublishS3PendingStatusWithFiles(shaText, "failed", []EcsDoneResultFile{}, outputLines)
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

	var resultFiles []EcsDoneResultFile
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

		resultFiles = append(resultFiles, EcsDoneResultFile{
			BucketKey: cacheBucketResultKey,
			CdnUrl:    GetCdnUrlBase() + "/get/" + cacheBucketResultKey,
		})
	}

	fmt.Printf("Upload completed, returning successful result\n")
	PublishS3PendingStatusWithFiles(shaText, "success", resultFiles, outputLines)
}
