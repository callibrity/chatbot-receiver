# Chatbot Receiver

Edge (Receiver) layer for [chatbot service](https://github.com/callibrity/chatbot-service) (including but not limited to):

- Request coming from Slack

Developed using [Gin](https://github.com/gin-gonic/gin) framework and [Grpc](https://github.com/grpc/grpc-go).

## Environment variable

To run locally, 2 environment variables have to be set up

- SLACK_SIGNING_SECRET
- BOT_USER_OAUTH_ACCESS_TOKEN

You should be able to get them from slack bot setting page.

## Start up
### Prerequisite
- [Golang](https://golang.org/doc/install)
- [Protobuf Complier](https://grpc.io/docs/protoc-installation/)
- [Go plugin](https://grpc.io/docs/protoc-installation/) for protobuf

### Install modules
```bash
go mod install
```

### Run application
Once all modules are installed, you can run the following command to start up the server locally,

```bash
go run main.go
```

or open project from Goland/Intellij (golang extension is needed), and start it from there. This will start
servert with `default` profile.

## Build docker image & run

To build a docker image, just run stand build command, you don't need to pass in the environment variables at build time.

Tu run the container you have ro specify these 2 environment variables, by passing
`-e SLACK_SIGNING_SECRET=<value> -e BOT_USER_OAUTH_ACCESS_TOKEN=$<value>` to the `docker run` command.

## Deploymentment

The deployment is handled by github action as described in the `master-tag.yml` file under `.github/workflows` folder.
