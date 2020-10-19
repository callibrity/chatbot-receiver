FROM golang:1.14

# environment variables for go
ENV GO111MODULE=on \
    CGO_ENABLED=0 \
    GOOS=linux \
    GOARCH=amd64 \
    GIN_MODE=release

# environment variables for app
ENV SLACK_SIGNING_SECRET=
ENV BOT_USER_OAUTH_ACCESS_TOKEN=

WORKDIR /build
COPY . .

RUN go mod download
RUN go build -o app .

WORKDIR /dist
COPY resources/application.yml ./resources/application.yml

RUN cp /build/app .
RUN rm -rf /build

EXPOSE 8080

CMD ["/dist/app", "--profile", "test"]
