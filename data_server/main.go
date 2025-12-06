package main

import (
	"data_server/internal/config"
	"data_server/internal/net"
)

func main() {
	cfg := config.LoadConfig()
	net.StartServer(&cfg)
}
