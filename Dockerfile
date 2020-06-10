FROM openjdk:8

ENV SLACK_SIGNING_SECRET=
ENV BOT_USER_OAUTH_ACCESS_TOKEN=

ENV spring_profiles_active=test

EXPOSE 8000

COPY build/libs/chatbot-receiver.jar /app/chatbot-receiver.jar

WORKDIR /app/

CMD ["java", "-jar", "chatbot-receiver.jar"]
