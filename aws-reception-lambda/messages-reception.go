package main

import (
	"encoding/json"
	"log"
)

type EcsDoneResultFile struct {
	BucketKey string `json:"key"`
	CdnUrl    string `json:"cnd_url"`
}

type RetryAfterTimeoutMessage struct {
	Type          string              `json:"type"`
	TimeoutMillis int                 `json:"timeout_millis"`
	Sha256        string              `json:"sha_256"`
	Reason        string              `json:"reason"`
	Files         []EcsDoneResultFile `json:"files"`
	Output        []string            `json:"log_output"`
}

func temporaryPayload(shaText string, reason string, logOutput []string) []byte {
	data, err := json.MarshalIndent(RetryAfterTimeoutMessage{
		Type:          "retry-after-timeout",
		TimeoutMillis: 777,
		Sha256:        shaText,
		Reason:        reason,
		Output:        logOutput,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}
	return data
}

func finalPayload(shaText string, reason string, files []EcsDoneResultFile, logOutput []string) []byte {
	data, err := json.MarshalIndent(RetryAfterTimeoutMessage{
		Type:          "final",
		TimeoutMillis: 0,
		Sha256:        shaText,
		Reason:        reason,
		Files:         files,
		Output:        logOutput,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}
	return data
}
