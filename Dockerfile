# Start from a Java 17 base image (or your Java version)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy the jar file into the container
COPY target/insurance-tracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port (optional, for documentation)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
