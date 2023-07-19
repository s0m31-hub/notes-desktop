package main

import (
	"fmt"
	"log"
	"os"
	"strings"
)

var javaBase string

func main() {
	locateJava()
}

func locateJava() {
	loc := os.Getenv("JAVA_HOME")
	if loc == "" {
		entries, err := os.ReadDir("./")
		if err == nil {
			for _, file := range entries {
				if file.IsDir() && strings.Contains(file.Name(), "jdk") {
					javaBase = "./" + file.Name()
					break
				}
			}
			if javaBase == "" {
				fmt.Println()
				fmt.Println("Did not detect installed java! Downloading openjdk 11")
				prevFile, err := os.Stat("./openjava.tar.gz")
				if err != nil {
					if os.IsNotExist(err) {

					} else {
						log.Fatal(err)
					}
				} else {
					err = os.Remove(prevFile.Name())
					if err != nil {
						log.Fatal(err)
					}
				}
			}
		} else {
			log.Fatal(err)
		}
	}
}
