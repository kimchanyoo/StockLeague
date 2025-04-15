FROM openjdk:17-jdk-slim

COPY build/libs/backend-0.0.1-SNAPSHOT.jar /usr/app/app.jar

ENTRYPOINT ["java", "-jar", "/usr/app/app.jar"]

EXPOSE 8080
