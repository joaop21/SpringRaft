# Pull server environment execution image
FROM openjdk:latest

# Modify the application jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Start executing jar
ENTRYPOINT ["java","-jar","/app.jar"]
