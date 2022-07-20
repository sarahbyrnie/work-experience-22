# syntax=docker/dockerfile:1
FROM openjdk:16-alpine3.13
WORKDIR /app
RUN mkdir -p /data/outputs
RUN mkdir -p /data/inputs
COPY src/Main.java /app/Main.java
RUN javac /app/Main.java
ENTRYPOINT ["java", "Main"]
#ENTRYPOINT ["tail", "-f", "/dev/null"]
