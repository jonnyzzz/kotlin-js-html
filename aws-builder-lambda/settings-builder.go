package main

import (
	"os"
)

func GetCacheBucketName() string {
	return os.Getenv("KTJS_BUCKET")
}

func GetShaRequest() string {
	return os.Getenv("KTJS_SHATEXT")
}

func GetTaskInput() string {
	return os.Getenv("KTJS_INPUT_KEY")
}

func GetCacheBucketResponsePath(sha string) string {
	return "v1/" + sha + "/result.json"
}

func GetCacheBucketInputPath(sha string) string {
	return "v1/" + sha + "/input.kt"
}

func GetCacheBucketResultFilePath(sha string, filename string) string {
	return "v1/" + sha + "/build/" + filename
}
