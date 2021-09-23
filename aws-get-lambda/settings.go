package main

import (
	"os"
)

func GetCacheBucketName() string {
	return os.Getenv("KTJS_BUCKET")
}
