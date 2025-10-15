# Use JDK 17 on Alpine
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install tesseract OCR and dependencies
RUN apk add --no-cache \
    tesseract-ocr \
    tesseract-ocr-data-eng \
    tesseract-ocr-data-osd \
    leptonica \
    libstdc++ \
    && mkdir -p /usr/share/tesseract-ocr/5/tessdata

# Copy app jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
