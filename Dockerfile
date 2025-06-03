# Stage 1: Build the application
FROM gradle:7.5.1-jdk17 AS build
WORKDIR /app

# Copy build files
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src/

# Build the JAR file
RUN gradle bootJar --stacktrace

# --- DEBUG LINE ADDED HERE ---
RUN ls -l build/libs/
# ---------------------------------

# Stage 2: Create the final, slim runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the 'build' stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Expose the port your application listens on
EXPOSE 8080

# Command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]
