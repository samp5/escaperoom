package env

import (
	"data_server/internal/service"
	"fmt"
	"net/http"
)

func RunLocal(svc *service.Service) {
	server := &http.Server{
		Addr:    fmt.Sprintf(":%d", svc.Cfg.Port),
		Handler: svc.Router,
	}
	server.ListenAndServe()
}
