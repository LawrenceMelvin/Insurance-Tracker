# Builder stage: build the JAR using Maven (uses a maintained Maven+JDK image)
FROM maven:3.9.4-eclipse-temurin-17 AS builder
# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Grant execute permission to the Maven wrapper
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/insurance-tracker-0.0.1-SNAPSHOT.jar"]