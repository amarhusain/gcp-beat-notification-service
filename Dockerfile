# Use a slim, stable Java base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged application JAR file into the container
# The `target/*.jar` assumes a Maven build. Adjust if using Gradle or another build tool.
COPY target/*.jar app.jar

# Expose the port on which the application will run
EXPOSE 8083

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]