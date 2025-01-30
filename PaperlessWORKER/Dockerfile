FROM openjdk:17-jdk-slim

# Installing Ghostscript and Tesseract
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng ghostscript

WORKDIR /app

# Copying the JAR-file
COPY ./target/PaperlessWORKER-0.0.1-SNAPSHOT.jar paperless-worker.jar


# Expose Port 8082 (optional, just for documentation porpuses)
EXPOSE 8082

# Execute the JAR-file
CMD ["java", "-jar", "paperless-worker.jar"]
