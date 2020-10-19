package config

import (
	"gopkg.in/yaml.v2"
	"io"
	"io/ioutil"
	"log"
	"os"
	"strings"
)

type ServerConfig struct {
	Port     string `yaml:"port"`
	BasePath string `yaml:"base-path"`
}

type SlackConfig struct {
	SigningSecret           string `yaml:"signing-secret"`
	BotUserOauthAccessToken string `yaml:"bot-user-oauth-access-token"`
}

type LoggingConfig struct {
	Level string `yaml:"level"`
}

type ChatBotServicePropertiesConfig struct {
	Host    string `yaml:"host"`
	Port    string `yaml:"port"`
	Timeout int8   `yaml:"timeout"`
}

type GrpcConfig struct {
	ChatBotServiceProperties ChatBotServicePropertiesConfig `yaml:"chatbot-service-properties"`
}

type Config struct {
	Profile string        `yaml:"profile"`
	Server  ServerConfig  `yaml:"server"`
	Slack   SlackConfig   `yaml:"slack"`
	Logging LoggingConfig `yaml:"logging"`
	Grpc    GrpcConfig    `yaml:"grpc"`
}

func New(activeProfile string) *Config {
	if strings.TrimSpace(activeProfile) == "" {
		activeProfile = "default"
		log.Println("Using default profile")
	}

	content, err := ioutil.ReadFile("resources/application.yml")
	if err != nil {
		panic(err)
	}

	expanded := os.ExpandEnv(string(content))
	decoder := yaml.NewDecoder(strings.NewReader(expanded))

	config := &Config{}
	for {
		if err = decoder.Decode(config); err != nil {
			break
		}

		if config.Profile == activeProfile {
			break
		}
	}

	if err != nil && err != io.EOF {
		panic(err)
	}

	return config
}
