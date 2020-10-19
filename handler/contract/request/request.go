package request

type SlackBotEventRequest struct {
	Type      string   `json:"type"`
	Challenge string   `json:"challenge"`
	Event     BotEvent `json:"event"`
}

type BotEvent struct {
	Type    string `json:"type"`
	SubType string `json:"subtype"`
	Text    string `json:"text"`
	User    string `json:"user"`
	Channel string `json:"channel"`
}

type SimpleChatRequest struct {
	Message string `json:"message"`
}
