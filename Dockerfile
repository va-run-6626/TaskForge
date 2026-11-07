# --- Stage 1: Build the application ---
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy the dependency definition file
COPY pom.xml .

# Download dependencies to cache them in the Docker layer
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application package, skipping tests for speed
RUN mvn clean package -DskipTests

# --- Stage 2: Create the runtime image ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd --system taskforge \
    && useradd --system \
       --gid taskforge \
       --no-create-home \
       --shell /usr/sbin/nologin \
       taskforge
USER taskforge

# Copy the compiled jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
