# Use full Debian-based JDK 17
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Install Tesseract 5 and dependencies
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    libleptonica-dev \
    libtesseract-dev \
    libgif-dev \
    libpng-dev \
    libjpeg-dev \
    libtiff-dev \
    libwebp-dev \
    libicu-dev \
    build-essential \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Set TESSDATA_PREFIX for Tesseract 5
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

# Copy your Spring Boot jar
COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
