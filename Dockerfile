FROM eclipse-temurin:17-jre

RUN adduser --system --group appuser

WORKDIR /app

COPY target/*.jar app.jar

USER appuser

EXPOSE 8080

CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]