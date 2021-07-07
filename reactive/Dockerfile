# Pull maven environment to build the app
FROM maven:3.8.1-openjdk-16 AS build

# Set the working directory
WORKDIR /app

# Copy the external folder to the /app folder
COPY . .

# Run the maven command to install the necessary dependencies
RUN mvn clean install -P JPA

###############################################3

# Pull server environment execution image
FROM openjdk:16-jdk-alpine

# Set the working directory
WORKDIR /app

# Modify the application jar
COPY --from=build /app/raft/target/*-exec.jar app.jar

# Expose ports
EXPOSE 8080

# Start executing jar
ENTRYPOINT ["java","-jar","app.jar"]
