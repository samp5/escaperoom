package config

import (
	"data_server/internal/log"
	"encoding/json"
	"os"
)

type Config struct {
	Env      string `json:"env"`
	Port     int    `json:"port"`
	S3Bucket string `json:"s3_bucket"`
	MongoDB  struct {
		DbName         string `json:"db_name"`
		Collections struct {
			MapMetadata string `json:"map_metadata"`
			PlayerRecords string `json:"player_records"`
		} `json:"collections"`
	} `json:"mongo_db"`
}

func LoadConfig() Config {
	cfg_file, err := os.Open("config/config.json")
	if err != nil {
		log.WithError(err).Fatal("error opening config")
	}

	var cfg Config
	if err = json.NewDecoder(cfg_file).Decode(&cfg); err != nil {
		log.WithError(err).Fatal("error decoding json into config")
	}

	return cfg
}
