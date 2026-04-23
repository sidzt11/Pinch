# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper and configuration files first (for dependency caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execution rights to the wrapper
RUN chmod +x gradlew

# Download dependencies (this step is cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build the application
COPY src src
RUN ./gradlew build -x test --no-daemon

# Stage 2: Create the runtime image with FFmpeg
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install FFmpeg and clean up apt cache to reduce image size
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify FFmpeg installation during build
RUN ffmpeg -version

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create the temporary storage directory and set permissions
RUN mkdir -p /tmp/compression-service && chmod 777 /tmp/compression-service

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
