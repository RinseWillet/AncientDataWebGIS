# Use Eclipse Temurin JRE 21 (modern, actively maintained OpenJDK distribution)
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from the previous stage
COPY ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar

# Expose application port
EXPOSE 8080

# Health check (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -version || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]
