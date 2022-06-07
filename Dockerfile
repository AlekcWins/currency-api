FROM alpine:3.13

RUN apk add openjdk11
COPY ./build/libs/currency-api-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]