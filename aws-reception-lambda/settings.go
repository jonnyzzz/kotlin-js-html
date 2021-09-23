package main

import (
	"os"
	"strings"
)

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

func GetCacheBucketResponsePath(sha string) string {
	return "v1/" + sha + "/result.json"
}
