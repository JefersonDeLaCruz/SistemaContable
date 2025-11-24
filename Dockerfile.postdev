# ====================================
# STAGE 1: Build Tailwind CSS
# ====================================
FROM node:20-alpine AS tailwind-builder

WORKDIR /tailwind

# Install Tailwind CSS and DaisyUI
RUN npm install -D tailwindcss @tailwindcss/cli daisyui

# Copy Tailwind configuration
COPY tailwind.config.js ./

# Copy source files for Tailwind processing
COPY backend/sic/src/main/resources/static/css/input.css ./css/
COPY backend/sic/src/main/resources/templates ./templates/
COPY backend/sic/src/main/resources/static/js ./js/

# Build Tailwind CSS (minified for production)
RUN npx tailwindcss -i ./css/input.css -o ./css/output.css --minify

# ====================================
# STAGE 2: Build Spring Boot Application
# ====================================
FROM maven:3.9-eclipse-temurin-17 AS maven-builder

WORKDIR /build

# Copy Maven wrapper and pom.xml for dependency caching
COPY backend/sic/mvnw backend/sic/mvnw.cmd ./
COPY backend/sic/.mvn ./.mvn
COPY backend/sic/pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/sic/src ./src

# Copy compiled Tailwind CSS from previous stage
COPY --from=tailwind-builder /tailwind/css/output.css ./src/main/resources/static/css/output.css

# Build the application (skip tests for production build)
RUN ./mvnw clean package -DskipTests -B

# ====================================
# STAGE 3: Runtime Image
# ====================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=maven-builder /build/target/sic-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

