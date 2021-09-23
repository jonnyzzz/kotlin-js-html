package main

import (
	"os"
	"strings"
)

func GetCdnUrlBase() string {
	return os.Getenv("KTJS_CDN_BASE")
}

func GetCacheBucketName() string {
	return os.Getenv("KTJS_BUCKET")
}

func GetEcsClusterName() string {
	return os.Getenv("KTJS_ECS_CLUSTER_NAME")
}

func GetEcsTaskDefinitionArn() string {
	return os.Getenv("KTJS_ECS_TASK_DEFINITION")
}

func GetEcsTaskSubnets() []string {
	subnets := os.Getenv("KTJS_ECS_TASK_SUBNETS")
	return strings.Split(subnets, ",")
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
