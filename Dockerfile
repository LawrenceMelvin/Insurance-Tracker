# ---- Step 1: Build the application ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy your app source code
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests

# ---- Step 2: Run the application ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Use port provided by environment (e.g., Render)
ENV PORT=8080
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
