# ==============================
# Stage 1: Build the JAR
# ==============================
# Use Maven with Java 21 to compile and package the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container for the build process
WORKDIR /app

# Copy the Maven descriptor first to leverage Docker’s caching for dependencies
COPY pom.xml .

# Copy the entire source code
COPY src ./src

# Build the application (skipping tests for faster builds)
RUN mvn clean package -DskipTests


# ==============================
# Stage 2: Run the JAR
# ==============================
# Use a lightweight JRE (no build tools) to run the Spring Boot app
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the runtime container
WORKDIR /app

# Copy the packaged JAR from the builder stage into the runtime container
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port (adjust if different in application.properties)
EXPOSE 8083

# Define the startup command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
