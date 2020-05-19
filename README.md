# Chatbot Receiver

Receiver (Edging) layer for [chatbot service](https://github.com/callibrity/chatbot-service) (including but not limited to):

- Request coming from Slack

Developed using spring boot starter with Grpc.

## Environment variable

To run locally, 2 environment variables have to be set up

- SLACK_SIGNING_SECRET
- BOT_USER_OAUTH_ACCESS_TOKEN

You should be able to get them from slack bot setting page.

## Start up

Once all dependances are installed, you can run the following command to start up the service locally,

```bash
./gradlew bootRun
```

or open project from Intellij, and start it from there.

## Build docker image & run

To build a docker image, just run stand build command, you don't need to pass in the environment variables at build time.

Tu run the container you have ro specify these 2 environment variables, by passing `-e SLACK_SIGNING_SECRET=<value> -e BOT_USER_OAUTH_ACCESS_TOKEN=$<value>` to the `docker run` command.

## Deploymentment

There is a kubernetes depolyment file `chatbot-receiver.yaml` which can be used to deploy it into GKE.
To use it replace the `image` field with the image tag you build. There is an example (commented out) in it.
Also make sure the environment where you try to deploy from using `kubectl` has the environment variables mentioned above setup.
Then you can use the following command to deploy:

```bash
envsubst < chatbot-receiver.yaml | kubectl apply -f -
```

Make sure `envsubst` in installed before you run it.
