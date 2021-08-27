# Pull server environment execution image
FROM openjdk:16-jdk-alpine

# Modify the application jar
ARG JAR_FILE=target/*-exec.jar
COPY ${JAR_FILE} app.jar

# Expose ports
EXPOSE 8080

# Start executing jar
ENTRYPOINT ["java","-jar","/app.jar"]