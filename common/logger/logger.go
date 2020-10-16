package logger

import (
	"fmt"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

var levelMap = map[string]zapcore.Level{
	"DEBUG": zapcore.DebugLevel,
	"INFO":  zapcore.InfoLevel,
	"WARN":  zapcore.WarnLevel,
	"ERROR": zapcore.ErrorLevel,
	"PANIC": zapcore.PanicLevel,
}

func New(level string) *zap.Logger {
	loggerLevel, ok := levelMap[level]
	if !ok {
		panic(fmt.Errorf("invalid level %s provided", level))
	}

	cfg := zap.Config{
		Encoding:         "json",
		Level:            zap.NewAtomicLevelAt(loggerLevel),
		OutputPaths:      []string{"stdout"},
		ErrorOutputPaths: []string{"stderr"},
		EncoderConfig: zapcore.EncoderConfig{
			MessageKey: "message",

			LevelKey:    "level",
			EncodeLevel: zapcore.CapitalLevelEncoder,

			TimeKey:    "time",
			EncodeTime: zapcore.ISO8601TimeEncoder,

			CallerKey:    "caller",
			EncodeCaller: zapcore.ShortCallerEncoder,
		},
	}

	logger, err := cfg.Build()
	if err != nil {
		panic(err)
	}

	return logger
}
