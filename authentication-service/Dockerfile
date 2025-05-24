# ---------- Build Stage ----------
FROM maven:3.8.5-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Make Maven wrapper executable (if you're using ./mvnw)
RUN chmod +x mvnw

# Build the application (skipping tests for faster build)
RUN ./mvnw clean package -DskipTests

# ---------- Run Stage ----------
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
