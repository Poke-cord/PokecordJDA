FROM gradle:6.8.1-jdk11 AS builder

WORKDIR /app
COPY . /app
RUN gradle shadowJar

FROM openjdk:11-buster
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
