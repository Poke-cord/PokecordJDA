FROM eclipse-temurin:20-jre-jammy
WORKDIR /app
COPY build/libs/*-all.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
