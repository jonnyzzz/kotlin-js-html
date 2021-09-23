package main

import (
	"encoding/json"
	"log"
)

type RetryAfterTimeoutMessage struct {
	Type          string `json:"type"`
	TimeoutMillis int    `json:"timeout_millis"`
	Sha256        string `json:"sha_256"`
	Reason        string `json:"reason"`
}

func temporaryPayload(shaText string, reason string) []byte {
	data, err := json.MarshalIndent(RetryAfterTimeoutMessage{
		Type:          "retry-after-timeout",
		TimeoutMillis: 777,
		Sha256:        shaText,
		Reason:        reason,
	}, "", "  ")

	if err != nil {
		log.Panic("Failed to serialize retry message", err.Error(), err)
	}
	return data
}
