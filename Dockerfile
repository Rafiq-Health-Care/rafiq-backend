# Use JDK 17 on Alpine
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install Tesseract OCR and dependencies
RUN apk add --no-cache \
    tesseract-ocr \
    tesseract-ocr-data-eng \
    tesseract-ocr-data-osd \
    leptonica \
    libstdc++ \
    gcc \
    g++ \
    libc6-compat

# Optional: Verify installation (debugging)
RUN tesseract --version

# Copy app jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
