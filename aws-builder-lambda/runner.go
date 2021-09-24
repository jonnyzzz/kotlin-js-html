package main

import (
	"bufio"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"sync"
	"syscall"
	"time"
)

type OutputCollector struct {
	buffer []string
	mutex  *sync.Mutex
}

func NewOutputCollector() *OutputCollector {
	return &OutputCollector{
		mutex:  &sync.Mutex{},
		buffer: make([]string, 0, 100),
	}
}

func (r *OutputCollector) PushLine(line string) {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	r.buffer = append(r.buffer, line)
}

func (r *OutputCollector) Results() []string {
	r.mutex.Lock()
	defer r.mutex.Unlock()
	return append([]string{}, r.buffer...)
}

func linesToChan(r io.Reader, collector *OutputCollector) {
	bufReader := bufio.NewReader(r)
	for {
		line, _, err := bufReader.ReadLine()

		if err != nil {
			return
		}

		logMessage := string(line)
		log.Println("GRADLE: ", logMessage)
		collector.PushLine(logMessage)
	}
}

func RunGradle(progressMessages chan []string) ([]string, error) {
	defer close(progressMessages)

	userHomeDir, _ := os.UserHomeDir()
	log.Println("User Home is set to ", userHomeDir)

	cmd := exec.Command("bash", "--login", "-c",
		"cd /runner && "+
			"./gradlew fullDistBuild --stacktrace")

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

	collector := NewOutputCollector()

	go func() {
		defer wg.Done()
		linesToChan(stdoutPipe, collector)
	}()

	go func() {
		defer wg.Done()
		linesToChan(stderrPipe, collector)
	}()

	isPumpRunning := true
	go func() {
		defer func() {
			recover()
		}()

		for {
			time.Sleep(time.Second / 2)
			if !isPumpRunning {
				return
			}

			select {
			case progressMessages <- collector.Results():
			default:
			}
		}
	}()

	wg.Wait()
	err = cmd.Wait()

	isPumpRunning = false

	if err != nil {
		if exiterr, ok := err.(*exec.ExitError); ok {
			// The program has exited with an exit code != 0

			// This works on both Unix and Windows. Although package
			// syscall is generally platform dependent, WaitStatus is
			// defined for both Unix and Windows and in both cases has
			// an ExitStatus() method with the same signature.
			if status, ok := exiterr.Sys().(syscall.WaitStatus); ok {
				log.Printf("Exit Status: %d", status.ExitStatus())
				collector.PushLine(fmt.Sprint("Process exited with code: ", status.ExitStatus()))
			}
		}

		log.Printf("cmd.Run() failed with %v\n", err)
	}

	return collector.Results(), err
}
