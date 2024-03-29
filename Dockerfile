FROM openjdk:11-buster
WORKDIR /app
COPY build/libs/*-all.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
