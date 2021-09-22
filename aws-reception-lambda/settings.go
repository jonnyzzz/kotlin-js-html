package main

import "os"

func GetCacheBucketName() string {
	return os.Getenv("KTJS_BUCKET")
}

func GetCacheBucketResponsePath(sha string) string {
	return "v1/" + sha + "/result.js"
}
