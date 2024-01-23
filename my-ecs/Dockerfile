# # Use the Maven image with OpenJDK 17 for building the application
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app
COPY . /app

RUN mvn clean install
# RUN mvn clean compile assembly:single;

# Use the Alpine-based JRE image for the runtime environment
FROM eclipse-temurin:17-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/ecs-1.0-SNAPSHOT.jar /app/ecs-1.0-SNAPSHOT.jar

# # Set the command to run your application
CMD ["java", "-jar", "ecs-1.0-SNAPSHOT.jar"]
# CMD ["java", "-cp", "ecs-1.0-SNAPSHOT.jar:aws-sdk-java.jar:.", "com.example.myecs.MyECSApp"]
# CMD ["java", "-cp", "ecs-1.0-SNAPSHOT.jar:.", "com.example.myecs.MyECSApp"]