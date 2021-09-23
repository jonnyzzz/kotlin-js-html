package main

import (
	"bufio"
	"io"
	"log"
	"os"
	"os/exec"
	"sync"
	"syscall"
)

func linesToChan(r io.Reader) {
	bufReader := bufio.NewReader(r)
	for {
		line, _, err := bufReader.ReadLine()

		if err != nil {
			return
		}

		logMessage := string(line)
		log.Println("GRADLE: ", logMessage)
	}
}

func RunGradle() error {
	userHomeDir, _ := os.UserHomeDir()
	log.Println("User Home is set to ", userHomeDir)

	cmd := exec.Command("bash", "--login", "-c",
		"cd /runner && "+
			"id $(whoami) && "+
			"mount && "+
			"ls -lah && "+
			"whoami && "+
			"df -h . && "+
			"./gradlew stage")

	cmd.Dir = "/runner"

	stdinPipe, err := cmd.StdinPipe()
	if err != nil {
		log.Fatal("Failed to open stdin pipe ", err.Error(), " ", err)
	}

	stdoutPipe, err := cmd.StdoutPipe()
	if err != nil {
		log.Fatal("Failed to open stdout pipe ", err.Error(), " ", err)
	}

	stderrPipe, err := cmd.StderrPipe()
	if err != nil {
		log.Fatal("Failed to open stderr pipe ", err.Error(), " ", err)
	}

	err = cmd.Start()
	if err != nil {
		log.Fatalf("cmd.Start() failed with '%s'\n", err)
	}

	//close stdin, just in case it would like to read
	err = stdinPipe.Close()

	var wg sync.WaitGroup
	wg.Add(2)

	go func() {
		defer wg.Done()
		linesToChan(stdoutPipe)
	}()

	go func() {
		defer wg.Done()
		linesToChan(stderrPipe)
	}()

	wg.Wait()

	err = cmd.Wait()
	if err != nil {
		if exiterr, ok := err.(*exec.ExitError); ok {
			// The program has exited with an exit code != 0

			// This works on both Unix and Windows. Although package
			// syscall is generally platform dependent, WaitStatus is
			// defined for both Unix and Windows and in both cases has
			// an ExitStatus() method with the same signature.
			if status, ok := exiterr.Sys().(syscall.WaitStatus); ok {
				log.Printf("Exit Status: %d", status.ExitStatus())
			}
		}

		log.Printf("cmd.Run() failed with %v\n", err)
	}

	return err
}
