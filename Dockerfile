# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src/

# Ensure gradlew is executable and convert line endings (important for Windows hosts)
RUN apt-get update && apt-get install -y dos2unix \
    && dos2unix gradlew \
    && chmod +x ./gradlew

# Build the JAR file
RUN ./gradlew bootJar

# --- NEW DEBUG LINE ADDED HERE ---
RUN ls -l /app/build/libs/
# ---------------------------------

# Stage 2: Create the final, slim runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the 'build' stage
# Adjust this path/name if the 'ls -l' output shows something different
COPY --from=build /app/build/libs/PersonsFinder-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port your application listens on
EXPOSE 8080

# Command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]