# ---- Step 1: Build the application ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy everything (including hidden files like .mvn)
COPY . .

# Give execute permission to mvnw
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# ---- Step 2: Run the application ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Let Render dynamically inject the PORT
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
