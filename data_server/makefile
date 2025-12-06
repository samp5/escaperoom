build: clean
	mkdir build
	env GOOS=linux GOARCH=arm64 go build -ldflags="-X 'data_server/internal/service.GinMode=release'" -o build/bootstrap
	mkdir build/config
	cp config/deploy.json build/config/config.json
	cd build; zip server.zip bootstrap config/config.json

infra:
	cd infrastructure; terraform fmt; terraform apply

deploy: build infra

clean:
	rm -rf build
