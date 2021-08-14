FROM openjdk:11-buster
WORKDIR /app
COPY build/libs/*-all.jar /app/app.jar
CMD ["java", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=1089", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-jar", "/app/app.jar"]
