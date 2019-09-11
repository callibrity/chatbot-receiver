FROM openjdk:8

ARG HOST_SLACK_SIGNING_SECRET
ARG HOST_BOT_USER_OAUTH_ACCESS_TOKEN

ENV SLACK_SIGNING_SECRET=$HOST_SLACK_SIGNING_SECRET
ENV BOT_USER_OAUTH_ACCESS_TOKEN=$HOST_BOT_USER_OAUTH_ACCESS_TOKEN

ENV spring_profiles_active=test

EXPOSE 8000

COPY build/libs/chatbot-receiver-0.0.1.jar /app/chatbot-receiver.jar

WORKDIR /app/

CMD ["java", "-jar", "chatbot-receiver.jar"]
