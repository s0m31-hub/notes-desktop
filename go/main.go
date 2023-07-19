package main

import (
	"github.com/walle/targz"
	"io"
	"log"
	"net/http"
	"os"
	"os/exec"
	"strings"
)

var javaBase string

func main() {
	locateJava()
	log.Println("Located Java")
	notes := exec.Command(javaBase+"/bin/java", "-jar", "notes.jar")
	log.Println(javaBase)
	notes.Stdout = os.Stdout
	notes.Stdin = os.Stdin
	notes.Stderr = os.Stderr
	notes.Run()
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
				log.Println("Java not found! Downloading openjdk11")
				prevFile, err := os.Stat("./openjava.tar.gz")
				if err != nil {
					if !os.IsNotExist(err) {
						log.Fatal(err)
					}
				} else {
					err = os.Remove(prevFile.Name())
					if err != nil {
						log.Fatal(err)
					}
				}
				file, err := os.Create("./openjava.tar.gz")
				if err != nil {
					log.Fatal(err)
				}
				defer file.Close()
				resp, err := http.Get("https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz")
				defer resp.Body.Close()
				_, err = io.Copy(file, resp.Body)
				if err != nil {
					log.Fatal(err)
				}
				log.Println("Extracting archive")
				err = targz.Extract("./openjava.tar.gz", ".")
				if err != nil {
					log.Fatal(err)
				}
				exec.Command("chmod", "+x", "jdk-11.0.2", "-R").Run()
				javaBase = "./jdk-11.0.2"
				log.Println("Cleaning up")
				os.Remove("openjava.tar.gz")
			}
		} else {
			log.Fatal(err)
		}
	} else {
		javaBase = loc
	}
}
