package log

import (
	"encoding/json"
	"os"
	"time"
)

type logger map[string]any

func WithError(e error) logger {
	l := make(logger, 1)
	l["error"] = e.Error()
	return l
}

func WithField(key string, value any) logger {
	l := make(logger, 1)
	l[key] = value
	return l
}

func (l logger) WithError(e error) logger {
	l["error"] = e.Error()
	return l
}

func (l logger) WithField(key string, value any) logger {
	l[key] = value
	return l
}

func (l logger) Debug() {
	l.log("debug")
}

func (l logger) Info(message string) {
	l["message"] = message
	l.log("info")
}

func (l logger) Error(message string) {
	l["message"] = message
	l.log("error")
}

func (l logger) Fatal(message string) {
	l["message"] = message
	l.log("fatal")
	os.Exit(1)
}

func (l logger) log(level string) {
	l["timestamp"] = time.Now().UTC().Format("2006-01-02T15:04:05")
	l["level"] = level

	str, err := json.Marshal(l)
	if err != nil {
		Error("error parsing log. investigate immediately.")
		return
	}

	println(string(str))
}

func Info(message string) {
	println("{\"level\":\"info\",\"msg\":\"" + message + "\"}")
}

func Error(message string) {
	println("{\"level\":\"error\",\"msg\":\"" + message + "\"}")
}

func Fatal(message string) {
	println("{\"level\":\"fatal\",\"msg\":\"" + message + "\"}")
	os.Exit(1)
}
